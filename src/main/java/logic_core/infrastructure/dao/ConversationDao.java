package logic_core.infrastructure.dao;

import Shared.Models.Conversation.Conversation;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ConversationDao extends AbstractJpaDao<Conversation>
{
    public ConversationDao(EntityManager entityManager)
    {
        super(entityManager, Conversation.class);
    }

    public List<Conversation> findConversationsByUserId(UUID userId)
    {
        Objects.requireNonNull(userId, "userId must not be null");

        return entityManager.createQuery(
                        "SELECT DISTINCT c FROM Conversation c " +
                                "JOIN c.members cm " +
                                "WHERE cm.user.id = :userId " +
                                "ORDER BY c.updatedAt DESC",
                        Conversation.class
                )
                .setParameter("userId", userId)
                .getResultList();
    }
}
