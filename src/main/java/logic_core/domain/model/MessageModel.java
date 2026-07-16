package logic_core.domain.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MessageModel
{
    private UUID messageId;
    private UUID conversationId;
    private UUID senderId;
    private UUID receiverId;
    private String content;
    private OffsetDateTime sentAt;
    private OffsetDateTime readAt;

    public boolean isRead()
    {
        return readAt != null;
    }

    public void markAsRead()
    {
        if (this.readAt == null)
        {
            this.readAt = OffsetDateTime.now();
        }
    }
}
