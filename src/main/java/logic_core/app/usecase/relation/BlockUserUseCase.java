package logic_core.app.usecase.relation;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.BlockUserRequest;
import logic_core.app.dto.response.BlockActionResponse;
import logic_core.app.dto.validator.BlockValidator;
import logic_core.app.mapper.BlockMapper;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.SessionUserContext;
import logic_core.common.exception.*;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.model.BlockRelation;
import logic_core.domain.policy.BlockPolicy;
import logic_core.domain.repository.RelationshipRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlockUserUseCase
{
    @NonNull private final BlockValidator validator;
    @NonNull private final BlockPolicy policy;
    @NonNull private final RelationshipRepository relationshipRepository;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<BlockActionResponse> execute(BlockUserRequest request)
    {
        if (request == null || request.blockedId() == null) {
            return Result.failure("Blocked user ID is required.");
        }

        try
        {
            SessionUserContext context =
                    lockOrchestrator.lockAndGetContextByToken(
                            request.sessionToken()
                    );

            UUID blockerId = context.lockedUser().getId();
            UUID blockedId = request.blockedId();

            validator.validate(blockerId, blockedId);
            policy.validateBlock(blockerId, blockedId);

            BlockRelation blockRelation = BlockRelation.create(blockerId, blockedId, timeProvider.now());
            relationshipRepository.saveBlock(blockRelation);

            removeFollowRelationIfExists(blockerId, blockedId);
            removeFollowRelationIfExists(blockedId, blockerId);


            return Result.success(BlockMapper.toBlockedResponse(blockRelation));
        }
        catch (AppException e)
        {
            return Result.failure(e.getMessage());
        }
        catch (Exception e)
        {
            return Result.failure("Failed to process block relationship.");
        }
    }

    private void removeFollowRelationIfExists(UUID followerId, UUID followingId)
    {
        relationshipRepository.findFollowRelation(followerId, followingId)
                .ifPresent(relationshipRepository::deleteFollow);
    }
}
