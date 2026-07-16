package logic_core.app.usecase.relation;

import logic_core.app.dto.request.FollowUserRequest;
import logic_core.app.dto.response.FollowResponse;
import logic_core.app.dto.validator.FollowValidator;
import logic_core.app.mapper.FollowMapper;
import logic_core.app.security.CurrentUserProvider;
import logic_core.common.exception.ConflictException;
import logic_core.common.exception.OperationNotAllowedException;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.Relationship.UserFollowedEvent;
import logic_core.domain.model.FollowRelation;
import logic_core.domain.policy.FollowPolicy;
import logic_core.domain.repository.RelationshipRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class FollowUserUseCase
{
    @NonNull private final FollowValidator validator;
    @NonNull private final FollowPolicy policy;
    @NonNull private final RelationshipRepository relationshipRepository;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final CurrentUserProvider currentUserProvider;
    @NonNull private final TimeProvider timeProvider;

    public Result<FollowResponse> execute(FollowUserRequest request)
    {
        UUID followerId = currentUserProvider.requireCurrentUserId();
        UUID followingId = request.followingId();

        try
        {
            validator.validate(followerId, followingId);
            policy.validateFollow(followerId, followingId);
        }
        catch (IllegalArgumentException | ConflictException | OperationNotAllowedException e)
        {
            return Result.failure(e.getMessage());
        }

        FollowRelation followRelation = createFollowRelation(followerId, followingId);
        relationshipRepository.saveFollow(followRelation);

        eventPublisher.publish(new UserFollowedEvent(
                followerId,
                followingId,
                timeProvider.now()
        ));

        long followersCount = relationshipRepository.countFollowers(followingId);
        FollowResponse response = FollowMapper.toResponse(true, followersCount);

        return Result.success(response);
    }

    private FollowRelation createFollowRelation(UUID followerId, UUID followingId)
    {
        return FollowRelation.create(
                followerId,
                followingId,
                timeProvider.now()
        );
    }
}
