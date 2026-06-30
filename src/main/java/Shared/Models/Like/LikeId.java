package Shared.Models.Like;

import Shared.Models.Tweet.Tweet;
import Shared.Models.User.User;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class LikeId implements Serializable
{
    private User user;
    private Tweet tweet;
}
