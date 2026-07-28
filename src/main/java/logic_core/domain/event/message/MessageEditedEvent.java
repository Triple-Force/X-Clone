package logic_core.domain.event.message;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class MessageEditedEvent extends DomainEvent implements MessageEvent
{
    private final UUID messageId;
    private final UUID conversationId;
    private final UUID actorId;
    private final String previousContent;
    private final String newContent;

    public MessageEditedEvent(UUID messageId,
                              UUID conversationId,
                              UUID actorId,
                              String previousContent,
                              String newContent,
                              OffsetDateTime occurredAt)
    {
        super(occurredAt);

        this.messageId = Objects.requireNonNull(messageId);
        this.conversationId = Objects.requireNonNull(conversationId);
        this.actorId = Objects.requireNonNull(actorId);
        this.previousContent = Objects.requireNonNull(previousContent);
        this.newContent = Objects.requireNonNull(newContent);
    }
}