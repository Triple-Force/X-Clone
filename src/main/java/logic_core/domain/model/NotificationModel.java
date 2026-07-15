package logic_core.domain.model;

import Shared.Models.Notification.NotificationType;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class NotificationModel
{
    private UUID id;
    private UUID recipientId;
    private UUID actorId;
    private UUID tweetId;
    private NotificationType type;
    private boolean isRead;
    private OffsetDateTime createdAt;

    public void markAsRead()
    {
        this.isRead = true;
    }

    public void markAsUnread()
    {
        this.isRead = false;
    }


    public boolean isUnread()
    {
        return !isRead;
    }


    public boolean hasTweetReference()
    {
        return tweetId != null;
    }


    public boolean isSelfTriggered()
    {
        return recipientId.equals(actorId);
    }


    public boolean isTweetInteraction()
    {
        if (type == null) return false;
        return type == NotificationType.LIKE ||
                type == NotificationType.REPLY ||
                type == NotificationType.RETWEET ||
                type == NotificationType.MENTION;
    }


    public boolean isFollowNotification()
    {
        return type == NotificationType.FOLLOW;
    }

    // --- Construction/Validation Utilities ---


    public static NotificationModel createNew(UUID id, UUID recipientId, UUID actorId, UUID tweetId, NotificationType type)
    {
        if (id == null || recipientId == null || type == null)
        {
            throw new IllegalArgumentException("Notification id, recipientId, and type must not be null");
        }

        return NotificationModel.builder()
                .id(id)
                .recipientId(recipientId)
                .actorId(actorId)
                .tweetId(tweetId)
                .type(type)
                .isRead(false)
                .createdAt(OffsetDateTime.now())
                .build();
    }
}
