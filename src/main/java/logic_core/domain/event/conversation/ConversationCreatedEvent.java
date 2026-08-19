package logic_core.domain.event.conversation;


import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
public class ConversationCreatedEvent extends DomainEvent implements ConversationEvent
{
    private final UUID conversationId;
    private final UUID creatorUserId;
    private final List<UUID> participantIds;

    public ConversationCreatedEvent(UUID conversationId,
                                    UUID creatorUserId,
                                    List<UUID> participantIds,
                                    OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.conversationId = Objects.requireNonNull(conversationId, "conversationId must not be null");
        this.creatorUserId = Objects.requireNonNull(creatorUserId, "creatorUserId must not be null");
        this.participantIds = List.copyOf(
                Objects.requireNonNull(participantIds, "participantIds must not be null")
        );
    }
}
