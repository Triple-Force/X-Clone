package logic_core.infrastructure.repository;

import logic_core.infrastructure.persistence.entity.conversationmember.ConversationMemberEntity;
import logic_core.infrastructure.persistence.entity.conversationmember.ConversationMemberEntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationMemberJpaRepository
        extends JpaRepository<ConversationMemberEntity, ConversationMemberEntityId> {

    /**
     * All membership rows of a conversation, oldest join first.
     * Mirrors the member materialization behind legacy
     * {@code ConversationDao.findByIdWithMembers(...)}: no user soft-delete
     * filter is applied here (the legacy findById path never filtered members by
     * user deletion — membership rows of soft-deleted users survive until the
     * user-deletion cascade removes them).
     */
    @Query("""
            SELECT cm
            FROM ConversationMemberEntity cm
            WHERE cm.conversation.id = :conversationId
            ORDER BY cm.joinedAt ASC
            """)
    List<ConversationMemberEntity> findByConversationId(@Param("conversationId") UUID conversationId);

    /**
     * Active membership relation between a conversation and a user.
     * Equivalent to legacy {@code ConversationMemberDao.findRelation(...)}
     * (conversation and user must both be non-deleted).
     */
    @Query("""
            SELECT cm
            FROM ConversationMemberEntity cm
            WHERE cm.conversation.id = :conversationId
              AND cm.user.id = :userId
              AND cm.conversation.isDeleted = false
              AND cm.user.isDeleted = false
            """)
    Optional<ConversationMemberEntity> findActiveRelation(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId);

    /**
     * Bulk hard-delete of all membership rows of a conversation. Used by the
     * conversation soft-delete cascade (legacy {@code Conversation.onSoftDelete()}
     * performed the equivalent CriteriaDelete).
     */
    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM ConversationMemberEntity cm WHERE cm.conversation.id = :conversationId")
    void deleteByConversationId(@Param("conversationId") UUID conversationId);
}
