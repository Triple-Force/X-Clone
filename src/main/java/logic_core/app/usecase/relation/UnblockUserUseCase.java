package logic_core.app.usecase.relation;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.UnblockUserRequest;
import logic_core.app.dto.response.BlockActionResponse;
import logic_core.app.dto.validator.BlockValidator;
import logic_core.app.mapper.BlockMapper;
import logic_core.app.security.AuthLockOrchestrator; // وارد کردن AuthLockOrchestrator
import logic_core.app.security.CurrentAuthContext;
import logic_core.app.security.SessionUserContext; // وارد کردن SessionUserContext
import logic_core.common.exception.AppException; // استفاده از AppException
import logic_core.common.exception.ConflictException;
import logic_core.common.exception.OperationNotAllowedException;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.Relationship.UserUnblockedEvent;
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
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final AuthLockOrchestrator lockOrchestrator; // تزریق AuthLockOrchestrator

    @Transactional
    public Result<BlockActionResponse> execute(UnblockUserRequest request)
    {
        if (request == null || request.unblockedId() == null) {
            return Result.failure("User ID to unblock is required.");
        }

        try
        {
            SessionUserContext context =
                    lockOrchestrator.lockAndGetContextByToken(
                            request.sessionToken()
                    );

            UUID blockerId = context.lockedUser().getId();
            UUID blockedId = request.unblockedId();

            validator.validate(blockerId, blockedId);
            policy.validateUnblock(blockerId, blockedId);

            relationshipRepository.findBlockRelation(blockerId, blockedId)
                    .ifPresent(blockRelation -> relationshipRepository.deleteBlock(blockRelation)); // استفاده از بلاک relation برای حذف

            eventPublisher.publish(new UserUnblockedEvent(
                    blockerId,
                    blockedId,
                    timeProvider.now()
            ));

            BlockActionResponse response = BlockMapper.toUnblockedResponse(blockerId, blockedId);
            return Result.success(response);
        }
        catch (AppException e)
        {
            return Result.failure(e.getMessage());
        }
        catch (Exception e)
        {
            return Result.failure("Failed to unblock user due to a system error.");
        }
    }
}
