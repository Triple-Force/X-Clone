package logic_core.app.mapper;

import logic_core.app.dto.response.NotificationResponse;
import logic_core.app.dto.response.UserResponse;
import logic_core.domain.model.NotificationModel;

public final class NotificationResponseMapper
{
    public NotificationResponse  toResponse(
            NotificationModel notification,
            UserResponse actor,
            String message
    )
    {
        return new NotificationResponse(
                notification.getId(),
                notification.getType().name(),
                actor,
                message,
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
