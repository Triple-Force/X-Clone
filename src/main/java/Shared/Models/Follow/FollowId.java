package Shared.Models.Follow;

import Shared.Models.User.User;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class FollowId implements Serializable
{
    private User follower;
    private User following;
}