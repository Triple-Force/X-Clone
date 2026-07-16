package logic_core.infrastructure.dao;

import Shared.Models.ConversationMember.ConversationMember;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ConversationMemberDao extends AbstractJpaDao<ConversationMember>
{
    public ConversationMemberDao(EntityManager entityManager)
    {
        super(entityManager, ConversationMember.class);
    }

    public Optional<ConversationMember> findRelation(UUID conversationId, UUID userId)
    {
        Objects.requireNonNull(conversationId, "conversationId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");

        List<ConversationMember> members = entityManager.createQuery(
                        "SELECT cm FROM ConversationMember cm " +
                                "WHERE cm.conversation.id = :conversationId " +
                                "AND cm.user.id = :userId",
                        ConversationMember.class
                )
                .setParameter("conversationId", conversationId)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getResultList();

        return members.stream().findFirst();
    }

    public List<ConversationMember> findByConversationId(UUID conversationId)
    {
        Objects.requireNonNull(conversationId, "conversationId must not be null");

        return entityManager.createQuery(
                        "SELECT cm FROM ConversationMember cm " +
                                "WHERE cm.conversation.id = :conversationId",
                        ConversationMember.class
                )
                .setParameter("conversationId", conversationId)
                .getResultList();
    }
}
