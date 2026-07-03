package Shared.Models.TweetEdit;

import Shared.Models.ImmutableEntity;
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
@Table(name = "tweet_edits", indexes = {@Index(name = "idx_tweet_edits_tweet_id", columnList = "tweet_id")})
public class TweetEdit extends ImmutableEntity
{
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tweet_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tweet tweet;

    @Column(name = "previous_content", nullable = false, length = 280)
    private String previousContent;
}
