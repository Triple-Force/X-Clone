package logic_core.app.dto.validator;

import logic_core.common.exception.ValidationException;

import java.util.*;
import java.util.stream.Collectors;

public class ConversationValidator
{
    private static final int MIN_PARTICIPANTS = 1;
    private static final int MAX_PARTICIPANTS = 50;

    public void validateCreate(UUID creatorId, List<UUID> participantIds)
    {
        requireNonNull(creatorId, "creatorId must not be null.");
        requireParticipantList(participantIds);

        List<UUID> normalized = normalizeParticipants(participantIds);

        if (normalized.size() < MIN_PARTICIPANTS)
        {
            throw new ValidationException("Conversation must contain at least one participant.");
        }

        if (normalized.size() > MAX_PARTICIPANTS)
        {
            throw new ValidationException("Conversation participants exceed maximum allowed size.");
        }

        if (normalized.contains(creatorId))
        {
            throw new ValidationException("creatorId must not be included in participantIds.");
        }
    }

    public void validateDelete(UUID actorId, UUID conversationId)
    {
        requireNonNull(actorId, "actorId must not be null.");
        requireNonNull(conversationId, "conversationId must not be null.");
    }

    public void validateAddMember(UUID actorId, UUID memberId, UUID conversationId)
    {
        requireNonNull(actorId, "actorId must not be null.");
        requireNonNull(memberId, "memberId must not be null.");
        requireNonNull(conversationId, "conversationId must not be null.");

        if (actorId.equals(memberId))
        {
            throw new ValidationException("actorId and memberId must not be the same.");
        }
    }

    public void validateDeleteMember(UUID actorId, UUID memberId, UUID conversationId)
    {
        requireNonNull(actorId, "actorId must not be null.");
        requireNonNull(memberId, "memberId must not be null.");
        requireNonNull(conversationId, "conversationId must not be null.");
    }

    private void requireParticipantList(List<UUID> participantIds)
    {
        if (participantIds == null)
        {
            throw new ValidationException("participantIds must not be null.");
        }

        if (participantIds.isEmpty())
        {
            throw new ValidationException("participantIds must not be empty.");
        }

        if (participantIds.stream().anyMatch(Objects::isNull))
        {
            throw new ValidationException("participantIds must not contain null values.");
        }

        Set<UUID> unique = new HashSet<>(participantIds);
        if (unique.size() != participantIds.size())
        {
            throw new ValidationException("participantIds must not contain duplicates.");
        }
    }

    private List<UUID> normalizeParticipants(List<UUID> participantIds)
    {
        return participantIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private void requireNonNull(Object value, String message)
    {
        if (value == null)
        {
            throw new ValidationException(message);
        }
    }
}
