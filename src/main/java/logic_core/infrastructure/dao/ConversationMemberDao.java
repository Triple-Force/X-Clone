package logic_core.infrastructure.dao;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.ConversationMember.ConversationMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ConversationMemberDao extends GenericDAO<ConversationMember>
{
    public ConversationMemberDao()
    {
        super(ConversationMember.class);
    }

    public ConversationMember save(ConversationMember conversationMember)
    {
        insert(conversationMember);
        return conversationMember;
    }

    public void updateConversationMember(ConversationMember conversationMember)
    {
        update(conversationMember);
    }

    public Optional<ConversationMember> findRelation(UUID conversationId, UUID userId)
    {
        if (conversationId == null || userId == null)
        {
            return Optional.empty();
        }

        return Optional.ofNullable(
                findOneByJpql(
                        """
                        SELECT cm
                        FROM ConversationMember cm
                        WHERE cm.conversation.id = :conversationId
                          AND cm.user.id = :userId
                          AND cm.conversation.isDeleted = false
                          AND cm.user.isDeleted = false
                        """,
                        query -> query
                                .setParameter("conversationId", conversationId)
                                .setParameter("userId", userId)
                )
        );
    }

    public List<ConversationMember> findByConversationId(UUID conversationId)
    {
        if (conversationId == null)
        {
            return List.of();
        }

        return findByJpql(
                """
                SELECT cm
                FROM ConversationMember cm
                JOIN FETCH cm.user
                WHERE cm.conversation.id = :conversationId
                  AND cm.conversation.isDeleted = false
                  AND cm.user.isDeleted = false
                ORDER BY cm.joinedAt ASC
                """,
                query -> query.setParameter("conversationId", conversationId)
        );
    }

    public List<ConversationMember> findByUserId(UUID userId)
    {
        if (userId == null)
        {
            return List.of();
        }

        return findByJpql(
                """
                SELECT cm
                FROM ConversationMember cm
                JOIN FETCH cm.conversation
                WHERE cm.user.id = :userId
                  AND cm.user.isDeleted = false
                  AND cm.conversation.isDeleted = false
                ORDER BY cm.joinedAt ASC
                """,
                query -> query.setParameter("userId", userId)
        );
    }

    public boolean exists(UUID conversationId, UUID userId)
    {
        return findRelation(conversationId, userId).isPresent();
    }

    public long countMembers(UUID conversationId)
    {
        if (conversationId == null)
        {
            return 0;
        }

        return countByJpql(
                """
                SELECT COUNT(cm)
                FROM ConversationMember cm
                WHERE cm.conversation.id = :conversationId
                  AND cm.conversation.isDeleted = false
                  AND cm.user.isDeleted = false
                """,
                query -> query.setParameter("conversationId", conversationId)
        );
    }

    public void deleteMember(UUID conversationId, UUID userId)
    {
        ConversationMember relation = findRelation(conversationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation member relation not found."));

        delete(relation);
    }
}