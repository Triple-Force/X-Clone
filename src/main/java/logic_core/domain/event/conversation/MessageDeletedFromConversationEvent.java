package logic_core.domain.event.conversation;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class MessageDeletedFromConversationEvent extends DomainEvent implements ConversationEvent
{
    private final UUID conversationId;
    private final UUID messageId;
    private final UUID actorId;

    public MessageDeletedFromConversationEvent(UUID conversationId,
                                               UUID messageId,
                                               UUID actorId,
                                               OffsetDateTime occurredAt
    )
    {
        super(occurredAt);
        this.conversationId = Objects.requireNonNull(conversationId, "conversationId must not be null");
        this.messageId = Objects.requireNonNull(messageId, "messageId must not be null");
        this.actorId = Objects.requireNonNull(actorId, "actorId must not be null.");
    }
}