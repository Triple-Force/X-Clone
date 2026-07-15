package logic_core.app.usecase.relation;

import logic_core.app.dto.request.BlockUserRequest;
import logic_core.app.dto.response.BlockActionResponse;
import logic_core.app.dto.validator.BlockValidator;
import logic_core.app.mapper.BlockMapper;
import logic_core.app.security.CurrentUserProvider;
import logic_core.common.exception.ConflictException;
import logic_core.common.exception.OperationNotAllowedException;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.Relationship.UserBlockedEvent;
import logic_core.domain.model.BlockRelation;
import logic_core.domain.policy.BlockPolicy;
import logic_core.domain.repository.RelationshipRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class BlockUserUseCase
{
    @NonNull private final BlockValidator validator;
    @NonNull private final BlockPolicy policy;
    @NonNull private final RelationshipRepository relationshipRepository;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final CurrentUserProvider currentUserProvider;

    public Result<BlockActionResponse> execute(BlockUserRequest request)
    {
        UUID blockerId = currentUserProvider.requireCurrentUserId();
        UUID blockedId = request.blockedId();

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
        relationshipRepository.saveBlock(blockRelation);

        eventPublisher.publish(new UserBlockedEvent(
                blockerId,
                blockedId,
                timeProvider.now()
        ));

        BlockActionResponse response = BlockMapper.toBlockedResponse(blockRelation);
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
