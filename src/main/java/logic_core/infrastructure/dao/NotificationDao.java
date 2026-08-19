package logic_core.infrastructure.dao;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Notification.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class NotificationDao extends GenericDAO<Notification>
{
    public NotificationDao()
    {
        super(Notification.class);
    }

    public List<Notification> findByReceiverId(UUID receiverId)
    {
        return findByJpql(
                """
                SELECT n
                FROM Notification n
                WHERE n.recipient.id = :receiverId
                  AND n.recipient.isDeleted = false
                ORDER BY n.createdAt DESC
                """,
                q -> q.setParameter("receiverId", receiverId)
        );
    }

    public List<Notification> findUnreadByReceiverId(UUID receiverId)
    {
        return findByJpql(
                """
                SELECT n
                FROM Notification n
                WHERE n.recipient.id = :receiverId
                  AND n.recipient.isDeleted = false
                  AND n.isRead = false
                ORDER BY n.createdAt DESC
                """,
                q -> q.setParameter("receiverId", receiverId)
        );
    }

    public int markAllAsRead(UUID receiverId)
    {
        List<Notification> unreadNotifications = findUnreadByReceiverId(receiverId);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        upsertAll(unreadNotifications);
        return unreadNotifications.size();
    }


    public Optional<Notification> findById(UUID notificationId)
    {
        return Optional.ofNullable(super.findById(notificationId));
    }

    public Notification save(Notification notification)
    {
        insert(notification);
        return notification;
    }


    public Notification update(Notification notification)
    {
        return super.update(notification);

    }
}
