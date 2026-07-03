package Shared.Models.TweetHashtag;

import Shared.Models.Hashtag.Hashtag;
import Shared.Models.Tweet.Tweet;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tweet_hashtags", indexes = {@Index(name = "idx_tweet_hashtags_hashtag_id", columnList = "hashtag_id")})
@IdClass(TweetHashtagId.class)
public class TweetHashtag
{
    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tweet_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tweet tweet;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hashtag_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Hashtag hashtag;

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private int usageCount = 0;
}
