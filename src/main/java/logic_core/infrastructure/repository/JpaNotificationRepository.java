package logic_core.infrastructure.repository;

import Shared.Models.Notification.Notification;
import jakarta.persistence.EntityManager;
import logic_core.domain.model.NotificationModel;
import logic_core.domain.repository.NotificationRepository;
import logic_core.infrastructure.dao.NotificationDao;
import logic_core.infrastructure.mapper.NotificationPersistenceMapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class JpaNotificationRepository implements NotificationRepository
{
    private final NotificationDao notificationDao;
    private final EntityManager entityManager;

    public JpaNotificationRepository(NotificationDao notificationDao, EntityManager entityManager)
    {
        this.notificationDao = Objects.requireNonNull(notificationDao, "NotificationDao must not be null");
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager must not be null");
    }

    @Override
    public Optional<NotificationModel> findById(UUID id)
    {
        return notificationDao.findById(id)
                .map(NotificationPersistenceMapper::toModel);
    }

    @Override
    public List<NotificationModel> findByReceiverId(UUID receiverId)
    {
        return notificationDao.findByReceiverId(receiverId).stream()
                .map(NotificationPersistenceMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationModel> findUnreadByReceiverId(UUID receiverId)
    {
        return notificationDao.findUnreadByReceiverId(receiverId).stream()
                .map(NotificationPersistenceMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public void save(NotificationModel domainModel)
    {
        Notification entity = NotificationPersistenceMapper.toPersistence(domainModel, entityManager);

        if (entity.getId() != null && notificationDao.findById(entity.getId()).isPresent())
        {
            notificationDao.update(entity);
        }
        else
        {
            notificationDao.save(entity);
        }
    }

    @Override
    public int markAllAsRead(UUID receiverId)
    {
        return notificationDao.markAllAsRead(receiverId);
    }

}
