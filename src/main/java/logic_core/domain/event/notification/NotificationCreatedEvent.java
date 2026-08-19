package logic_core.domain.event.notification;

import Shared.Models.Notification.NotificationType;
import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class NotificationCreatedEvent extends DomainEvent implements NotificationEvent
{

    UUID notificationId;
    UUID receiverId;
    UUID actorId;
    NotificationType type;
    UUID targetEntityId;

    public NotificationCreatedEvent(UUID notificationId,
                                    UUID receiverId,
                                    UUID actorId,
                                    NotificationType type,
                                    UUID targetEntityId,
                                    OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.notificationId = Objects.requireNonNull(notificationId);
        this.receiverId = Objects.requireNonNull(receiverId);
        this.actorId = Objects.requireNonNull(actorId);
        this.type = Objects.requireNonNull(type);
        this.targetEntityId = Objects.requireNonNull(targetEntityId);
    }
}
