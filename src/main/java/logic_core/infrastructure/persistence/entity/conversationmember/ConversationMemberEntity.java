package logic_core.infrastructure.persistence.entity.conversationmember;

import jakarta.persistence.*;
import logic_core.infrastructure.persistence.entity.UserEntity;
import logic_core.infrastructure.persistence.entity.conversation.ConversationEntity;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;

/**
 * Spring Data JPA entity for the {@code conversation_members} join table.
 * <p>
 * Membership rows are never soft-deleted (the table has no {@code is_deleted}
 * column) — adding is an insert, removing is a hard delete. Composite key is
 * {@code (conversation_id, user_id)}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "conversation_members", indexes = {
        @Index(name = "idx_conversation_members_user_id", columnList = "user_id")
})
@IdClass(ConversationMemberEntityId.class)
public class ConversationMemberEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ConversationEntity conversation;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private OffsetDateTime joinedAt;
}
