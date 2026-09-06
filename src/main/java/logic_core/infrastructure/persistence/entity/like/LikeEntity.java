package logic_core.infrastructure.persistence.entity.like;

import jakarta.persistence.*;
import logic_core.infrastructure.persistence.entity.tweet.TweetEntity;
import logic_core.infrastructure.persistence.entity.UserEntity;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "likes", indexes = {
    @Index(name = "idx_likes_tweet_id", columnList = "tweet_id")
})
@IdClass(LikeEntityId.class)
public class LikeEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tweet_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TweetEntity tweet;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
