package logic_core.infrastructure.persistence.entity.directmessage;

import jakarta.persistence.*;
import logic_core.infrastructure.persistence.entity.UserEntity;
import lombok.*;
import logic_core.infrastructure.persistence.base.MutableEntity;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "direct_messages", indexes = {
        @Index(name = "idx_direct_messages_conversation_id_created_at",
                columnList = "conversation_id, created_at"),
        @Index(name = "idx_direct_messages_sender_id", columnList = "sender_id"),
        @Index(name = "idx_direct_messages_is_read", columnList = "is_read")
})
public class DirectMessageEntity extends MutableEntity {

    /**
     * Conversation FK stored as a plain column reference.
     * <p>
     * Conversation persistence is not migrated yet, so no {@code ConversationEntity}
     * exists to map a {@code @ManyToOne} against. The legacy DAO filters on
     * {@code conversation.isDeleted = false}, but that condition is unreachable:
     * {@code Conversation.onSoftDelete()} hard-deletes all messages of a
     * conversation in the same transaction, and the DB FK is
     * {@code ON DELETE CASCADE}, so any row present in {@code direct_messages}
     * always belongs to a non-deleted conversation.
     */
    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity sender;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_read", nullable = false)
    @ColumnDefault("false")
    private boolean isRead = false;

    @Column(name = "is_edited", nullable = false)
    @ColumnDefault("false")
    private boolean isEdited = false;
}
