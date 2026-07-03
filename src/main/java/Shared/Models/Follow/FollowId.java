package Shared.Models.Follow;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class FollowId implements Serializable
{
    private UUID follower;
    private UUID following;
}
