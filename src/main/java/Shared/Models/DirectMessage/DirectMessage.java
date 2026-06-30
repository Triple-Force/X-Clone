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
@Table(name = "direct_messages")
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
}
