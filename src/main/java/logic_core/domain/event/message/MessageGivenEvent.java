package logic_core.domain.event.message;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class MessageGivenEvent extends DomainEvent implements MessageEvent
{
    private final UUID messageId;
    private final UUID conversationId;
    private final UUID requesterId;

    public MessageGivenEvent(UUID messageId,
                             UUID conversationId,
                             UUID requesterId,
                             OffsetDateTime occurredAt)
    {
        super(occurredAt);

        this.messageId = Objects.requireNonNull(messageId);
        this.conversationId = Objects.requireNonNull(conversationId);
        this.requesterId = Objects.requireNonNull(requesterId);
    }
}