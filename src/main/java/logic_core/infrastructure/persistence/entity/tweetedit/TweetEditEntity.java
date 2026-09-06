package logic_core.infrastructure.persistence.entity.tweetedit;

import jakarta.persistence.*;
import logic_core.infrastructure.persistence.entity.tweet.TweetEntity;
import lombok.*;
import logic_core.infrastructure.persistence.base.BaseEntity;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tweet_edits", indexes = {@Index(name = "idx_tweet_edits_tweet_id", columnList = "tweet_id")})
public class TweetEditEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tweet_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TweetEntity tweet;

    @Column(name = "previous_content", nullable = false, length = 280)
    private String previousContent;
}
