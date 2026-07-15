package logic_core.infrastructure.dao;

import Shared.Models.DirectMessage.DirectMessage;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DirectMessageDao extends AbstractJpaDao<DirectMessage>
{
    public DirectMessageDao(EntityManager entityManager)
    {
        super(entityManager, DirectMessage.class);
    }

    public List<DirectMessage> findByConversationId(UUID conversationId)
    {
        Objects.requireNonNull(conversationId, "conversationId must not be null");

        return entityManager.createQuery(
                        "SELECT dm FROM DirectMessage dm " +
                                "WHERE dm.conversation.id = :conversationId " +
                                "ORDER BY dm.createdAt ASC",
                        DirectMessage.class
                )
                .setParameter("conversationId", conversationId)
                .getResultList();
    }

    public List<DirectMessage> findLatestMessage(UUID conversationId, int limit)
    {
        Objects.requireNonNull(conversationId, "conversationId must not be null");

        return entityManager.createQuery(
                        "SELECT dm FROM DirectMessage dm " +
                                "WHERE dm.conversation.id = :conversationId " +
                                "ORDER BY dm.createdAt DESC",
                        DirectMessage.class
                )
                .setParameter("conversationId", conversationId)
                .setMaxResults(limit)
                .getResultList();
    }
}
