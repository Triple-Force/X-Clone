package Shared.Models.TweetHashtag;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TweetHashtagId implements Serializable
{
    private UUID tweet;
    private UUID hashtag;
}
