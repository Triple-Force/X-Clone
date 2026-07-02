package Shared.Models.DirectMessage;

import Shared.Models.Conversation.Conversation;
import Shared.Models.MutableEntity;
import Shared.Models.User.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "direct_messages", indexes = {@Index(name = "idx_direct_messages_conversation_id_created_at",
        columnList = "conversation_id, created_at"), @Index(name = "idx_direct_messages_sender_id",
        columnList = "sender_id"), @Index(name = "idx_direct_messages_is_read", columnList = "is_read")})
public class DirectMessage extends MutableEntity
{
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User sender;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_read", nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private boolean isRead = false;

    @Column(name = "is_edited", nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private boolean isEdited = false;

    @Override
    public void redact()
    {
        this.content = "This message has been deleted.";
    }
}
