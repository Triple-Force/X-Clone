package Shared.Models.TweetHashtag;

import Shared.Models.Hashtag.Hashtag;
import Shared.Models.Tweet.Tweet;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TweetHashtagId implements Serializable
{
    private Tweet tweet;
    private Hashtag hashtag;
}