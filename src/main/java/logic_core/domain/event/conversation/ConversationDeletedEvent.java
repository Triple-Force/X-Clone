package logic_core.domain.event.conversation;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
public class ConversationDeletedEvent extends DomainEvent implements ConversationEvent
{
    private final UUID conversationId;
    private final UUID actorId;
    private final List<UUID> participantIds;

    public ConversationDeletedEvent(UUID conversationId,
                                    UUID actorId,
                                    List<UUID> participantIds,
                                    OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.conversationId = Objects.requireNonNull(conversationId, "conversationId must not be null");
        this.actorId = Objects.requireNonNull(actorId, "actorId must not be null");
        this.participantIds = List.copyOf(
                Objects.requireNonNull(participantIds, "participantIds must not be null")
        );
    }
}
