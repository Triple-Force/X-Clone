package logic_core.domain.event.message;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class MessageDeletedEvent extends DomainEvent implements MessageEvent
{
    private final UUID messageId;
    private final UUID conversationId;
    private final UUID actorId;

    public MessageDeletedEvent(UUID messageId,
                               UUID conversationId,
                               UUID actorId,
                               OffsetDateTime occurredAt)
    {
        super(occurredAt);

        this.messageId = Objects.requireNonNull(messageId);
        this.conversationId = Objects.requireNonNull(conversationId);
        this.actorId = Objects.requireNonNull(actorId);
    }
}