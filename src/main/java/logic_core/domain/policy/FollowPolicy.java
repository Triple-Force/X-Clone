package logic_core.domain.policy;

import logic_core.common.exception.ConflictException;
import logic_core.common.exception.ForbiddenException;
import logic_core.common.exception.NotFoundException;
import logic_core.domain.model.UserModel;
import logic_core.domain.repository.RelationshipRepository;
import logic_core.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.UUID;


@RequiredArgsConstructor
public class FollowPolicy
{
    @NonNull
    private final UserRepository userRepository;

    @NonNull
    private final RelationshipRepository relationshipRepository;


    public void validateFollow(UUID followerId, UUID followingId)
    {
        requireNonNullId(followerId, "followerId");
        requireNonNullId(followingId, "followingId");
        ensureNotSelfAction(followerId, followingId, "Users cannot follow themselves.");

        UserModel follower = getExistingUser(followerId, "Follower user not found.");
        UserModel following = getExistingUser(followingId, "Target user not found.");

        ensureActiveUser(follower, "Inactive or deleted users cannot follow others.");
        ensureUserCanBeTargeted(following, "Cannot follow an inactive or deleted user.");
        ensureNoBlockBarrier(followerId, followingId, "Follow is not allowed because a block relation exists between users.");

        if (relationshipRepository.isFollowing(followerId, followingId))
        {
            throw new ConflictException("Follow relation already exists.");
        }
    }

    public void validateUnfollow(UUID followerId, UUID followingId)
    {
        requireNonNullId(followerId, "followerId");
        requireNonNullId(followingId, "followingId");
        ensureNotSelfAction(followerId, followingId, "Users cannot unfollow themselves.");

        UserModel follower = getExistingUser(followerId, "Follower user not found.");
        UserModel following = getExistingUser(followingId, "Target user not found.");

        ensureActiveUser(follower, "Inactive or deleted users cannot unfollow others.");
        ensureUserExistsAsTarget(following, "Target user not found.");

        if (!relationshipRepository.isFollowing(followerId, followingId))
        {
            throw new NotFoundException("Follow relation does not exist.");
        }
    }

    private UserModel getExistingUser(UUID userId, String notFoundMessage)
    {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(notFoundMessage));
    }

    private void ensureActiveUser(UserModel user, String message)
    {
        if (user.isDeleted() || !user.isActive())
        {
            throw new ForbiddenException(message);
        }
    }

    private void ensureUserCanBeTargeted(UserModel user, String message)
    {
        if (user.isDeleted() || !user.isActive())
        {
            throw new ForbiddenException(message);
        }
    }

    private void ensureUserExistsAsTarget(UserModel user, String message)
    {
        if (user == null || user.isDeleted())
        {
            throw new NotFoundException(message);
        }
    }

    private void ensureNoBlockBarrier(UUID sourceUserId, UUID targetUserId, String message)
    {
        if (relationshipRepository.existsBlockRelation(sourceUserId, targetUserId))
        {
            throw new ForbiddenException(message);
        }
    }

    private void ensureNotSelfAction(UUID actorId, UUID targetId, String message)
    {
        if (actorId.equals(targetId))
        {
            throw new ForbiddenException(message);
        }
    }

    private void requireNonNullId(UUID id, String name)
    {
        Objects.requireNonNull(id, name + " must not be null");
    }
}
