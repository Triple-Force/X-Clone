package logic_core.domain.event.message;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class MessageSentEvent extends DomainEvent implements MessageEvent
{
    private final UUID messageId;
    private final UUID conversationId;
    private final UUID senderId;
    private final String previewContent;

    public MessageSentEvent(UUID messageId,
                            UUID conversationId,
                            UUID senderId,
                            String previewContent,
                            OffsetDateTime occurredAt)
    {
        super(occurredAt);

        this.messageId = Objects.requireNonNull(messageId);
        this.conversationId = Objects.requireNonNull(conversationId);
        this.senderId = Objects.requireNonNull(senderId);
        this.previewContent = Objects.requireNonNull(previewContent);
    }
}