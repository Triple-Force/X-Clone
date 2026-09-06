package logic_core.infrastructure.repository;

import jakarta.persistence.LockModeType;
import logic_core.infrastructure.persistence.entity.directmessage.DirectMessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DirectMessageJpaRepository extends JpaRepository<DirectMessageEntity, UUID> {

    /**
     * Active lookup: message itself not soft-deleted and its sender not soft-deleted.
     * Equivalent to the legacy {@code DirectMessageDao.findById(...)} query.
     */
    @Query("""
            SELECT dm
            FROM DirectMessageEntity dm
            WHERE dm.id = :id
              AND dm.isDeleted = false
              AND dm.sender.isDeleted = false
            """)
    Optional<DirectMessageEntity> findActiveById(@Param("id") UUID id);

    /**
     * Same semantics as {@link #findActiveById(UUID)} but with a pessimistic
     * write lock (equivalent to legacy {@code DirectMessageDao.findByIdForUpdate(...)}).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT dm
            FROM DirectMessageEntity dm
            WHERE dm.id = :id
              AND dm.isDeleted = false
              AND dm.sender.isDeleted = false
            """)
    Optional<DirectMessageEntity> findActiveByIdForUpdate(@Param("id") UUID id);

    /**
     * All active messages of a conversation, oldest first.
     * Equivalent to legacy {@code DirectMessageDao.findByConversationId(UUID)}.
     */
    @Query("""
            SELECT dm
            FROM DirectMessageEntity dm
            WHERE dm.conversationId = :conversationId
              AND dm.isDeleted = false
              AND dm.sender.isDeleted = false
            ORDER BY dm.createdAt ASC
            """)
    List<DirectMessageEntity> findActiveByConversationId(@Param("conversationId") UUID conversationId);

    /**
     * Paged variant of {@link #findActiveByConversationId(UUID)}.
     * Equivalent to legacy {@code DirectMessageDao.findByConversationId(UUID, int, int)}.
     */
    @Query("""
            SELECT dm
            FROM DirectMessageEntity dm
            WHERE dm.conversationId = :conversationId
              AND dm.isDeleted = false
              AND dm.sender.isDeleted = false
            ORDER BY dm.createdAt ASC
            """)
    List<DirectMessageEntity> findActiveByConversationId(
            @Param("conversationId") UUID conversationId,
            Pageable pageable);

    /**
     * Same semantics as {@link #findActiveByConversationId(UUID)} but with a
     * pessimistic write lock (equivalent to legacy
     * {@code DirectMessageDao.findByConversationIdForUpdate(UUID)}).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT dm
            FROM DirectMessageEntity dm
            WHERE dm.conversationId = :conversationId
              AND dm.isDeleted = false
              AND dm.sender.isDeleted = false
            ORDER BY dm.createdAt ASC
            """)
    List<DirectMessageEntity> findActiveByConversationIdForUpdate(
            @Param("conversationId") UUID conversationId);

    /**
     * Newest {@code limit} active messages of a conversation, newest first.
     * Equivalent to legacy {@code DirectMessageDao.findLatestMessage(UUID, int)}.
     */
    @Query("""
            SELECT dm
            FROM DirectMessageEntity dm
            WHERE dm.conversationId = :conversationId
              AND dm.isDeleted = false
              AND dm.sender.isDeleted = false
            ORDER BY dm.createdAt DESC
            """)
    List<DirectMessageEntity> findLatestActiveByConversationId(
            @Param("conversationId") UUID conversationId,
            Pageable pageable);

    /**
     * Counts messages in a conversation that were sent by someone other than the
     * receiver and have not been read.
     * Equivalent to legacy {@code DirectMessageDao.countUnreadMessages(UUID, UUID)}.
     */
    @Query("""
            SELECT COUNT(dm)
            FROM DirectMessageEntity dm
            WHERE dm.conversationId = :conversationId
              AND dm.sender.id <> :receiverUserId
              AND dm.isRead = false
              AND dm.isDeleted = false
              AND dm.sender.isDeleted = false
            """)
    long countUnreadMessages(
            @Param("conversationId") UUID conversationId,
            @Param("receiverUserId") UUID receiverUserId);

    /**
     * Bulk hard-delete of all messages of a conversation. Used by the conversation
     * soft-delete cascade to reproduce the legacy
     * {@code Conversation.onSoftDelete()} behavior (direct messages are physically
     * removed, not redacted, when their conversation is deleted).
     */
    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM DirectMessageEntity dm WHERE dm.conversationId = :conversationId")
    void hardDeleteByConversationId(@Param("conversationId") UUID conversationId);
}
