package logic_core.infrastructure.dao;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Conversation.Conversation;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ConversationDao extends GenericDAO<Conversation>
{
    public ConversationDao()
    {
        super(Conversation.class);
    }

    public Conversation save(Conversation conversation)
    {
        insert(conversation);
        return conversation;

    }

    public Conversation insertConversation(Conversation conversation)
    {
        insert(conversation);
        return conversation;
    }

    public void updateConversation(Conversation conversation)
    {
        update(conversation);
    }

    public Optional<Conversation> findById(UUID id)
    {
        if (id == null)
        {
            return Optional.empty();
        }

        return Optional.ofNullable(
                findOneByJpql(
                        "SELECT c FROM Conversation c " +
                                "WHERE c.id = :id AND c.isDeleted = false",
                        query -> query.setParameter("id", id)
                )
        );
    }

    public Optional<Conversation> findByIdForUpdate(UUID id)
    {
        if (id == null)
        {
            return Optional.empty();
        }

        return Optional.ofNullable(
                findOneByJpql(
                        "SELECT c FROM Conversation c " +
                                "WHERE c.id = :id AND c.isDeleted = false",
                        query -> query.setParameter("id", id)
                                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                )
        );
    }

    public Optional<Conversation> findByIdWithMembers(UUID id)
    {
        List<Conversation> result =
                findByJpql(
                        """
                        SELECT DISTINCT c
                        FROM Conversation c
                        LEFT JOIN FETCH c.members cm
                        LEFT JOIN FETCH cm.user
                        WHERE c.id = :id
                          AND c.isDeleted = false
                        """,
                        q -> q.setParameter("id", id)
                );

        return result.stream().findFirst();
    }

    public Optional<Conversation> findByIdWithMembersForUpdate(UUID id)
    {
        if (id == null)
        {
            return Optional.empty();
        }

        return Optional.ofNullable(
                findOneByJpql(
                        """
                        SELECT DISTINCT c
                        FROM Conversation c
                        LEFT JOIN FETCH c.members cm
                        LEFT JOIN FETCH cm.user u
                        WHERE c.id = :id
                          AND c.isDeleted = false
                          AND (u IS NULL OR u.isDeleted = false)
                        """,
                        query -> query
                                .setParameter("id", id)
                                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                )
        );
    }

    public List<Conversation> findConversationsByUserId(UUID userId)
    {
        if (userId == null)
        {
            return List.of();
        }

        return findByJpql(
                """
                SELECT DISTINCT c
                FROM Conversation c
                LEFT JOIN FETCH c.members cmFetch
                LEFT JOIN FETCH cmFetch.user u
                WHERE c.isDeleted = false
                  AND (u IS NULL OR u.isDeleted = false)
                  AND c.id IN (
                        SELECT cm.conversation.id
                        FROM ConversationMember cm
                        WHERE cm.user.id = :userId
                          AND cm.user.isDeleted = false
                          AND cm.conversation.isDeleted = false
                  )
                ORDER BY c.updatedAt DESC
                """,
                query -> query.setParameter("userId", userId)
        );
    }

    public Conversation findDirectConversationBetween(
            UUID firstUserId,
            UUID secondUserId)
    {
        return findOneByJpql(
                """
                SELECT c
                FROM Conversation c
                WHERE c.isDeleted = false
                  AND (
                        SELECT COUNT(cm)
                        FROM ConversationMember cm
                        WHERE cm.conversation = c
                          AND cm.user.isDeleted = false
                  ) = 2
                  AND EXISTS (
                        SELECT 1
                        FROM ConversationMember cm
                        WHERE cm.conversation = c
                          AND cm.user.id = :firstUserId
                          AND cm.user.isDeleted = false
                  )
                  AND EXISTS (
                        SELECT 1
                        FROM ConversationMember cm
                        WHERE cm.conversation = c
                          AND cm.user.id = :secondUserId
                          AND cm.user.isDeleted = false
                  )
                """,
                q -> q.setParameter("firstUserId", firstUserId)
                        .setParameter("secondUserId", secondUserId)
        );
    }
}