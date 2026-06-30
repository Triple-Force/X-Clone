package Shared.Models.TweetMention;

import Shared.Models.Tweet.Tweet;
import Shared.Models.User.User;
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
@Table(name = "tweet_mentions")
@IdClass(TweetMentionId.class)
public class TweetMention
{
    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tweet_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tweet tweet;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentioned_user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User mentionedUser;
}
