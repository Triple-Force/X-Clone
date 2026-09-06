package logic_core.infrastructure.persistence.entity.tweet;

import jakarta.persistence.*;
import logic_core.infrastructure.persistence.entity.UserEntity;
import logic_core.infrastructure.persistence.entity.like.LikeEntity;
import lombok.*;
import logic_core.infrastructure.persistence.base.MutableEntity;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tweets")
public class TweetEntity extends MutableEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private UserEntity author;

    @Column(name = "content", length = 280)
    private String content;

    @Column(name = "published_at", nullable = false)
    @Builder.Default
    private OffsetDateTime publishedAt = OffsetDateTime.now();

    @Column(name = "scheduled_at")
    private OffsetDateTime scheduledAt;

    @Column(name = "is_pinned", nullable = false)
    @ColumnDefault("false")
    private boolean isPinned = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private TweetEntity replyTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retweet_of_id")
    private TweetEntity retweetOf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_of_id")
    private TweetEntity quoteOf;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "tweet", fetch = FetchType.LAZY)
    private List<LikeEntity> likes;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "replyTo", fetch = FetchType.LAZY)
    private List<TweetEntity> replies;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "retweetOf", fetch = FetchType.LAZY)
    private List<TweetEntity> retweets;
}
