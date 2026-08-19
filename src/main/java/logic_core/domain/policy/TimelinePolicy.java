package logic_core.domain.policy;

import logic_core.common.exception.ForbiddenException;
import logic_core.common.exception.NotFoundException;
import logic_core.domain.model.UserModel;
import logic_core.domain.repository.TimelineType;
import logic_core.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class TimelinePolicy
{
    @NonNull
    private final UserRepository userRepository;

    public void validateTimeline(
            TimelineType timelineType,
            UUID actorId,
            UUID targetUserId)
    {
        requireNonNullId(actorId, "actorId");

        UserModel actor = userRepository.findById(actorId)
                .orElseThrow(() ->
                        new NotFoundException("Actor not found."));

        if (!actor.isActive())
        {
            throw new ForbiddenException(
                    "Inactive users cannot view timelines.");
        }

        switch (timelineType)
        {
            case HOME:
            case FOLLOWING:
                return;

            case USER:
            case REPLIES:
            case MEDIA:
            case LIKED:

                requireNonNullId(targetUserId, "targetUserId");

                UserModel target = userRepository.findById(targetUserId)
                        .orElseThrow(() ->
                                new NotFoundException("Target user not found."));

                if (!target.isActive())
                {
                    throw new ForbiddenException(
                            "Cannot view timeline of an inactive user.");
                }

                return;
        }
    }

    private void requireNonNullId(
            UUID id,
            String field)
    {
        Objects.requireNonNull(
                id,
                field + " must not be null");
    }
}