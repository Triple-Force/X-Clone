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
public class MutePolicy
{
    @NonNull
    private final UserRepository userRepository;

    @NonNull
    private final RelationshipRepository relationshipRepository;

    public void validateMute(UUID muterId, UUID mutedId)
    {
        requireNonNullId(muterId, "mutedId");
        requireNonNullId(mutedId, "mutedID");
        ensureNotSelfAction(muterId, mutedId, "Users cannot mute themselves.");

        UserModel muter = getExistingUser(muterId, "Muter user not found.");
        UserModel muted = getExistingUser(mutedId, "Target user not found.");

        ensureUserCanAct(muter, "Inactive or deleted users cannot mute others.");
        ensureUserCanBeTargeted(muted, "Cannot mute an inactive or deleted user.");

        if (relationshipRepository.isMutedBy(muterId, mutedId))
        {
            throw new ConflictException("Mute relation already exists.");
        }
    }

    public void validateUnmute(UUID actorId, UUID targetId)
    {
        requireNonNullId(actorId, "mutedId");
        requireNonNullId(targetId, "mutedID");
        ensureNotSelfAction(actorId, targetId, "Users cannot unmute themselves.");

        UserModel muter = getExistingUser(actorId, "Muter user not found.");
        UserModel muted = getExistingUser(targetId, "Target user not found.");

        ensureUserCanAct(muter, "Inactive or deleted users cannot unmute others.");
        ensureUserExistsAsTarget(muted, "Target user not found.");

        if (!relationshipRepository.isMutedBy(actorId, targetId))
        {
            throw new NotFoundException("Mute relation does not exist.");
        }
    }

    private UserModel getExistingUser(UUID userId, String notFoundMessage)
    {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(notFoundMessage));
    }

    private void ensureUserCanAct(UserModel user, String message)
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
