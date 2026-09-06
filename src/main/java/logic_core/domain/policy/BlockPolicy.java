package logic_core.domain.policy;

import logic_core.common.exception.ConflictException;
import logic_core.common.exception.ForbiddenException;
import logic_core.common.exception.NotFoundException;
import logic_core.domain.model.UserModel;
import logic_core.domain.repository.RelationshipRepository;
import logic_core.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BlockPolicy
{
    private final UserRepository userRepository;
    private final RelationshipRepository relationshipRepository;

    public void validateBlock(UUID actorId, UUID targetId)
    {
        requireNonNullId(actorId, "blockerId");
        requireNonNullId(targetId, "blockedId");
        ensureNotSelfAction(actorId, targetId, "Cannot block yourself.");

        UserModel blocker = getExistingUser(actorId, "Blocker user not found.");
        UserModel blocked = getExistingUser(targetId, "Target user not found.");

        ensureActiveUser(blocker, "Inactive or deleted users cannot block others.");
        ensureActiveUser(blocked, "Cannot block an inactive or deleted user.");

        if (relationshipRepository.isBlockedBy(actorId, targetId))
        {
            throw new ConflictException("Block relation already exists.");
        }
    }

    public void validateUnblock(UUID actorId, UUID targetId)
    {
        requireNonNullId(actorId, "blockerId");
        requireNonNullId(targetId, "blockedId");
        ensureNotSelfAction(actorId, targetId, "Cannot unblock yourself.");

        UserModel blocker = getExistingUser(actorId, "Blocker user not found.");
        UserModel blocked = getExistingUser(targetId, "Target user not found.");

        ensureActiveUser(blocker, "Inactive or deleted users cannot unblock others.");
        ensureActiveUser(blocked, "Target user not found.");

        if (!relationshipRepository.isBlockedBy(actorId, targetId))
        {
            throw new NotFoundException("Block relation not found.");
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