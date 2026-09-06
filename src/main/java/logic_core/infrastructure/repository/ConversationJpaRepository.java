package logic_core.infrastructure.repository;

import jakarta.persistence.LockModeType;
import logic_core.infrastructure.persistence.entity.conversation.ConversationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationJpaRepository extends JpaRepository<ConversationEntity, UUID> {

    /**
     * Active lookup by id.
     * Equivalent to the legacy {@code ConversationDao.findById(...)} predicate.
     */
    @Query("""
            SELECT c
            FROM ConversationEntity c
            WHERE c.id = :id
              AND c.isDeleted = false
            """)
    Optional<ConversationEntity> findActiveById(@Param("id") UUID id);

    /**
     * Same predicate as {@link #findActiveById(UUID)} with a pessimistic write
     * lock (equivalent to legacy {@code ConversationDao.findByIdForUpdate(...)}).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c
            FROM ConversationEntity c
            WHERE c.id = :id
              AND c.isDeleted = false
            """)
    Optional<ConversationEntity> findActiveByIdForUpdate(@Param("id") UUID id);

    /**
     * Active conversations the user is an active member of, newest first.
     * Membership is expressed with an {@code EXISTS} subquery over
     * {@code ConversationMemberEntity} so no row multiplication occurs.
     * Equivalent to legacy {@code ConversationDao.findConversationsByUserId(...)}
     * (1-based page semantics applied by the caller via {@link Pageable}).
     */
    @Query("""
            SELECT c
            FROM ConversationEntity c
            WHERE c.isDeleted = false
              AND EXISTS (
                    SELECT 1
                    FROM ConversationMemberEntity cm
                    WHERE cm.conversation.id = c.id
                      AND cm.user.id = :userId
                      AND cm.user.isDeleted = false
              )
            ORDER BY c.updatedAt DESC
            """)
    List<ConversationEntity> findConversationsByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Count of active conversations the user is an active member of.
     * Equivalent to legacy {@code ConversationDao.countConversations(...)}; the
     * {@code EXISTS} shape keeps the filter identical to
     * {@link #findConversationsByUserId(UUID, Pageable)} so pagination
     * ({@code hasNext}) stays correct.
     */
    @Query("""
            SELECT COUNT(c)
            FROM ConversationEntity c
            WHERE c.isDeleted = false
              AND EXISTS (
                    SELECT 1
                    FROM ConversationMemberEntity cm
                    WHERE cm.conversation.id = c.id
                      AND cm.user.id = :userId
                      AND cm.user.isDeleted = false
              )
            """)
    long countConversations(@Param("userId") UUID userId);

    /**
     * Finds the single direct (exactly 2 active members) conversation between two
     * users. Preserves the legacy semantics: active conversation whose active
     * member count is 2 and where both users are active members. No uniqueness
     * constraint is introduced (duplicate detection stays application-level).
     */
    @Query("""
            SELECT c
            FROM ConversationEntity c
            WHERE c.isDeleted = false
              AND (
                    SELECT COUNT(cm)
                    FROM ConversationMemberEntity cm
                    WHERE cm.conversation.id = c.id
                      AND cm.user.isDeleted = false
              ) = 2
              AND EXISTS (
                    SELECT 1
                    FROM ConversationMemberEntity cm
                    WHERE cm.conversation.id = c.id
                      AND cm.user.id = :firstUserId
                      AND cm.user.isDeleted = false
              )
              AND EXISTS (
                    SELECT 1
                    FROM ConversationMemberEntity cm
                    WHERE cm.conversation.id = c.id
                      AND cm.user.id = :secondUserId
                      AND cm.user.isDeleted = false
              )
            """)
    Optional<ConversationEntity> findDirectConversationBetween(
            @Param("firstUserId") UUID firstUserId,
            @Param("secondUserId") UUID secondUserId);

    /**
     * Whether an active conversation with the given id exists.
     */
    boolean existsByIdAndIsDeletedFalse(UUID id);
}
