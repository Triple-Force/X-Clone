package Shared.Models.Notification;

import Shared.Models.ImmutableEntity;
import Shared.Models.Tweet.Tweet;
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
@Table(name = "notifications", indexes = {@Index(name = "idx_notifications_recipient_id_is_read",
        columnList = "recipient_id, is_read"), @Index(name = "idx_notifications_actor_id",
        columnList = "actor_id"), @Index(name = "idx_notifications_tweet_id", columnList = "tweet_id")})
public class Notification extends ImmutableEntity
{
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tweet_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tweet tweet;

    @Column(name = "is_read", nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private boolean isRead = false;
}
