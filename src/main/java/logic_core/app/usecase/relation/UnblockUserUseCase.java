package logic_core.app.usecase.relation;

import logic_core.app.dto.request.UnblockUserRequest;
import logic_core.app.dto.response.BlockActionResponse;
import logic_core.app.dto.validator.BlockValidator;
import logic_core.app.mapper.BlockMapper;
import logic_core.app.security.CurrentUserProvider;
import logic_core.common.exception.ConflictException;
import logic_core.common.exception.OperationNotAllowedException;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.Relationship.UserUnblockedEvent;
import logic_core.domain.model.BlockRelation;
import logic_core.domain.policy.BlockPolicy;
import logic_core.domain.repository.RelationshipRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UnblockUserUseCase
{
    @NonNull private final BlockValidator validator;
    @NonNull private final BlockPolicy policy;
    @NonNull private final RelationshipRepository relationshipRepository;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final CurrentUserProvider currentUserProvider;
    @NonNull private final TimeProvider timeProvider;

    public Result<BlockActionResponse> execute(UnblockUserRequest request)
    {
        UUID blockerId = currentUserProvider.requireCurrentUserId();
        UUID blockedId = request.unblockedId();

        try
        {
            validator.validate(blockerId, blockedId);
            policy.validateBlock(blockerId, blockedId);
        }
        catch (IllegalArgumentException | ConflictException | OperationNotAllowedException e)
        {
            return Result.failure(e.getMessage());
        }

        BlockRelation blockRelation = createBlockRelation(blockerId, blockedId);
        relationshipRepository.deleteBlock(blockRelation);

        eventPublisher.publish(new UserUnblockedEvent(
                blockerId,
                blockedId,
                timeProvider.now()
        ));

        BlockActionResponse response = BlockMapper.toUnblockedResponse(blockerId, blockedId);
        return Result.success(response);
    }

    private BlockRelation createBlockRelation(UUID blockerId, UUID blockedId)
    {
        return BlockRelation.create(
                blockerId,
                blockedId,
                timeProvider.now()
        );
    }
}
