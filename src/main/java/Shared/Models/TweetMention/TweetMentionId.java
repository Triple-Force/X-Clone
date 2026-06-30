package Shared.Models.TweetMention;

import Shared.Models.Tweet.Tweet;
import Shared.Models.User.User;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TweetMentionId implements Serializable
{
    private Tweet tweet;
    private User mentionedUser;
}
