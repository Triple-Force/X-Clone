package logic_core.domain.repository;

import logic_core.domain.model.NotificationModel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository
{
    Optional<NotificationModel> findById(UUID id);

    List<NotificationModel> findByReceiverId(UUID receiverId);

    List<NotificationModel> findUnreadByReceiverId(UUID receiverId);

    void save(NotificationModel notificationModel);

    int markAllAsRead(UUID receiverId);
}
