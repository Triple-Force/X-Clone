package logic_core.infrastructure.repository;

import Shared.Models.DirectMessage.DirectMessage;
import jakarta.persistence.EntityManager;
import logic_core.domain.model.MessageModel;
import logic_core.domain.repository.DirectMessageRepository;
import logic_core.infrastructure.dao.DirectMessageDao;
import logic_core.infrastructure.mapper.DirectMessagePersistenceMapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class JpaDirectMessageRepository implements DirectMessageRepository
{
    private final DirectMessageDao dao;
    private final EntityManager entityManager;

    public JpaDirectMessageRepository(DirectMessageDao dao, EntityManager entityManager)
    {
        this.dao = Objects.requireNonNull(dao, "directMessageDao must not be null.");
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager must not be null.");
    }

    @Override
    public MessageModel save(MessageModel model)
    {
        Objects.requireNonNull(model, "messageModel must not be null.");

        DirectMessage entity = DirectMessagePersistenceMapper.toPersistence(model, entityManager);
        DirectMessage savedEntity = dao.save(entity);

        return DirectMessagePersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public void update(MessageModel model)
    {
        Objects.requireNonNull(model, "messageModel must not be null.");
        Objects.requireNonNull(model.getMessageId(), "messageId must not be null.");

        dao.findById(model.getMessageId()).ifPresent(entity -> {
            DirectMessagePersistenceMapper.updateEntity(entity, model, entityManager);
            dao.updateMessage(entity);
        });
    }

    @Override
    public void softDelete(UUID messageId)
    {
        if (messageId == null)
        {
            return;
        }

        dao.findById(messageId).ifPresent(dao::delete);
    }

    @Override
    public Optional<MessageModel> findById(UUID id)
    {
        return dao.findById(id)
                .map(DirectMessagePersistenceMapper::toDomain);
    }

    @Override
    public Optional<MessageModel> findByIdForUpdate(UUID id)
    {
        return dao.findByIdForUpdate(id)
                .map(DirectMessagePersistenceMapper::toDomain);
    }

    @Override
    public List<MessageModel> findByConversationId(UUID conversationId)
    {
        return mapList(dao.findByConversationId(conversationId));
    }

    @Override
    public List<MessageModel> findByConversationIdForUpdate(UUID conversationId)
    {
        return mapList(dao.findByConversationIdForUpdate(conversationId));
    }

    @Override
    public List<MessageModel> findLatestMessages(UUID conversationId, int limit)
    {
        return mapList(dao.findLatestMessage(conversationId, limit));
    }

    @Override
    public long countUnreadMessages(UUID conversationId, UUID receiverUserId)
    {
        return dao.countUnreadMessages(conversationId, receiverUserId);
    }

    private static List<MessageModel> mapList(List<DirectMessage> entities)
    {
        if (entities == null || entities.isEmpty())
        {
            return List.of();
        }

        return entities.stream()
                .map(DirectMessagePersistenceMapper::toDomain)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageModel> findByConversationId(
            UUID conversationId,
            int page,
            int pageSize)
    {
        if (conversationId == null)
        {
            return List.of();
        }

        int offset = Math.max(0, (page - 1) * pageSize);

        return dao.findByConversationId(
                        conversationId,
                        pageSize,
                        offset)
                .stream()
                .map(DirectMessagePersistenceMapper::toDomain)
                .toList();
    }

    public DirectMessage findLastMessage(UUID conversationId)
    {
        return dao.findLastMessage(conversationId);
    }
}