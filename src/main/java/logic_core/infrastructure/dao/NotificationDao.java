package logic_core.infrastructure.dao;

import Shared.Models.Notification.Notification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import logic_core.common.exception.DatabaseException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class NotificationDao extends AbstractJpaDao<Notification>
{
    public NotificationDao(EntityManager entityManager)
    {
        super(entityManager, Notification.class);
    }

    public List<Notification> findByReceiverId(UUID receiverId)
    {
        Objects.requireNonNull(receiverId, "receiverId must not be null");

        return entityManager.createQuery(
                        "SELECT n FROM Notification n WHERE n.recipient.id = :receiverId ORDER BY n.createdAt DESC",
                        Notification.class
                )
                .setParameter("receiverId", receiverId)
                .getResultList();
    }

    public List<Notification> findUnreadByReceiverId(UUID receiverId)
    {
        Objects.requireNonNull(receiverId, "receiverId must not be null");

        return entityManager.createQuery(
                        "SELECT n FROM Notification n WHERE n.recipient.id = :receiverId AND n.isRead = false ORDER BY n.createdAt DESC",
                        Notification.class
                )
                .setParameter("receiverId", receiverId)
                .getResultList();
    }

    public int markAllAsRead(UUID receiverId)
    {
        Objects.requireNonNull(receiverId, "receiverId must not be null");

        EntityTransaction transaction = entityManager.getTransaction();

        try
        {
            transaction.begin();

            int updatedCount = entityManager.createQuery("""
                update Notification n
                set n.isRead = true
                where n.recipient.id = :receiverId
                  and n.isRead = false
                """)
                    .setParameter("receiverId", receiverId)
                    .executeUpdate();

            transaction.commit();
            return updatedCount;
        }
        catch (Exception e)
        {
            if (transaction.isActive())
            {
                transaction.rollback();
            }

            throw new DatabaseException("Failed to mark notifications as read.", e);
        }
    }


    public Optional<Notification> findById(UUID notificationId)
    {
        return super.findById(notificationId);
    }

    public Notification save(Notification notification)
    {
        Objects.requireNonNull(notification, "notification must not be null");

        EntityTransaction transaction = entityManager.getTransaction();

        try
        {
            transaction.begin();
            entityManager.persist(notification);
            transaction.commit();
            return notification;
        }
        catch (Exception e)
        {
            if (transaction.isActive())
            {
                transaction.rollback();
            }

            throw new DatabaseException("Failed to save notification.", e);
        }
    }


    public Notification update(Notification notification)
    {
        Objects.requireNonNull(notification, "notification must not be null");

        EntityTransaction transaction = entityManager.getTransaction();

        try
        {
            transaction.begin();
            Notification mergedNotification = entityManager.merge(notification);
            transaction.commit();
            return mergedNotification;
        }
        catch (Exception e)
        {
            if (transaction.isActive())
            {
                transaction.rollback();
            }

            throw new DatabaseException("Failed to update notification.", e);
        }
    }

}
