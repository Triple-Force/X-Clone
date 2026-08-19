package Shared.Models.TweetMention;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TweetMentionId implements Serializable
{
    private UUID tweet;
    private UUID mentionedUser;
}
