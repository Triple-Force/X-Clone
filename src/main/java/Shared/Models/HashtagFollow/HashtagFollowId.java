package Shared.Models.HashtagFollow;

import Shared.Models.Hashtag.Hashtag;
import Shared.Models.User.User;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class HashtagFollowId implements Serializable
{
    private User user;
    private Hashtag hashtag;
}
