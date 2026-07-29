package logic_core.infrastructure.dao;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.DirectMessage.DirectMessage;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DirectMessageDao extends GenericDAO<DirectMessage>
{
    public DirectMessageDao()
    {
        super(DirectMessage.class);
    }

    public DirectMessage save(DirectMessage entity)
    {
        insert(entity);
        return entity;
    }

    public void updateMessage(DirectMessage entity)
    {
        update(entity);
    }

    public Optional<DirectMessage> findById(UUID id)
    {
        if (id == null)
        {
            return Optional.empty();
        }

        return Optional.ofNullable(
                findOneByJpql(
                        """
                        SELECT dm
                        FROM DirectMessage dm
                        WHERE dm.id = :id
                          AND dm.isDeleted = false
                          AND dm.conversation.isDeleted = false
                          AND dm.sender.isDeleted = false
                        """,
                        q -> q.setParameter("id", id)
                )
        );
    }

    public Optional<DirectMessage> findByIdForUpdate(UUID id)
    {
        if (id == null)
        {
            return Optional.empty();
        }

        return Optional.ofNullable(
                findOneByJpql(
                        """
                        SELECT dm
                        FROM DirectMessage dm
                        WHERE dm.id = :id
                          AND dm.isDeleted = false
                          AND dm.conversation.isDeleted = false
                          AND dm.sender.isDeleted = false
                        """,
                        q -> q.setParameter("id", id)
                                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                )
        );
    }

    public List<DirectMessage> findByConversationId(UUID conversationId)
    {
        if (conversationId == null)
        {
            return List.of();
        }

        return findByJpql(
                """
                SELECT dm
                FROM DirectMessage dm
                WHERE dm.conversation.id = :conversationId
                  AND dm.isDeleted = false
                  AND dm.conversation.isDeleted = false
                  AND dm.sender.isDeleted = false
                ORDER BY dm.createdAt ASC
                """,
                q -> q.setParameter("conversationId", conversationId)
        );
    }

    public List<DirectMessage> findByConversationId(
            UUID conversationId,
            int limit,
            int offset)
    {
        if (conversationId == null)
        {
            return List.of();
        }

        return findByJpql(
                """
                SELECT dm
                FROM DirectMessage dm
                WHERE dm.conversation.id = :conversationId
                  AND dm.isDeleted = false
                  AND dm.conversation.isDeleted = false
                  AND dm.sender.isDeleted = false
                ORDER BY dm.createdAt ASC
                """,
                q ->
                {
                    q.setParameter("conversationId", conversationId);
                    q.setFirstResult(Math.max(0, offset));
                    q.setMaxResults(Math.max(1, limit));
                }
        );
    }

    public List<DirectMessage> findByConversationIdForUpdate(UUID conversationId)
    {
        if (conversationId == null)
        {
            return List.of();
        }

        return findByJpql(
                """
                SELECT dm
                FROM DirectMessage dm
                WHERE dm.conversation.id = :conversationId
                  AND dm.isDeleted = false
                  AND dm.conversation.isDeleted = false
                  AND dm.sender.isDeleted = false
                ORDER BY dm.createdAt ASC
                """,
                q -> q.setParameter("conversationId", conversationId)
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
        );
    }

    public List<DirectMessage> findLatestMessage(UUID conversationId, int limit)
    {
        if (conversationId == null)
        {
            return List.of();
        }

        return findByJpql(
                """
                SELECT dm
                FROM DirectMessage dm
                WHERE dm.conversation.id = :conversationId
                  AND dm.isDeleted = false
                  AND dm.conversation.isDeleted = false
                  AND dm.sender.isDeleted = false
                ORDER BY dm.createdAt DESC
                """,
                q -> q.setParameter("conversationId", conversationId)
                        .setMaxResults(limit)
        );
    }

    public long countUnreadMessages(UUID conversationId, UUID receiverUserId)
    {
        if (conversationId == null || receiverUserId == null)
        {
            return 0;
        }

        return countByJpql(
                """
                SELECT COUNT(dm)
                FROM DirectMessage dm
                WHERE dm.conversation.id = :conversationId
                  AND dm.sender.id <> :receiverUserId
                  AND dm.isRead = false
                  AND dm.isDeleted = false
                  AND dm.conversation.isDeleted = false
                  AND dm.sender.isDeleted = false
                """,
                q -> q.setParameter("conversationId", conversationId)
                        .setParameter("receiverUserId", receiverUserId)
        );
    }

    public DirectMessage findLastMessage(UUID conversationId)
    {
        if (conversationId == null)
        {
            return null;
        }

        return findOneByJpql(
                """
                SELECT dm
                FROM DirectMessage dm
                WHERE dm.conversation.id = :conversationId
                  AND dm.isDeleted = false
                  AND dm.conversation.isDeleted = false
                  AND dm.sender.isDeleted = false
                ORDER BY dm.createdAt DESC
                """,
                q -> q.setParameter("conversationId", conversationId)
                        .setMaxResults(1)
        );
    }
}