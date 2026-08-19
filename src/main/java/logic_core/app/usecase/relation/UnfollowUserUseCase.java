package logic_core.app.usecase.relation;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.UnfollowUserRequest;
import logic_core.app.dto.response.FollowResponse;
import logic_core.app.dto.validator.FollowValidator;
import logic_core.app.mapper.FollowMapper;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.CurrentAuthContext;
import logic_core.app.security.SessionUserContext;
import logic_core.common.exception.AppException;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.Relationship.UserUnfollowedEvent;
import logic_core.domain.policy.FollowPolicy;
import logic_core.domain.repository.RelationshipRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UnfollowUserUseCase
{
    @NonNull private final FollowValidator validator;
    @NonNull private final FollowPolicy policy;
    @NonNull private final RelationshipRepository relationshipRepository;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<FollowResponse> execute(UnfollowUserRequest request)
    {
        if (request == null || request.unfollowedId() == null) {
            return Result.failure("Unfollowed ID is required.");
        }

        try
        {
            SessionUserContext context =
                    lockOrchestrator.lockAndGetContextByToken(
                            request.sessionToken()
                    );

            UUID unfollowerId = context.lockedUser().getId();;
            UUID unfollowedId = request.unfollowedId();

            validator.validate(unfollowerId, unfollowedId);
            policy.validateUnfollow(unfollowerId, unfollowedId);

            relationshipRepository.findFollowRelation(unfollowerId, unfollowedId)
                    .ifPresent(relationshipRepository::deleteFollow);

            long followersCount = relationshipRepository.countFollowers(unfollowedId);

            eventPublisher.publish(new UserUnfollowedEvent(
                    unfollowerId,
                    unfollowedId,
                    timeProvider.now()
            ));

            return Result.success(FollowMapper.toResponse(false, followersCount));
        }
        catch (AppException e)
        {
            return Result.failure(e.getMessage());
        }
        catch (Exception e)
        {
            return Result.failure("Failed to unfollow user due to a system error.");
        }
    }
}
