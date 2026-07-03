package Shared.Models.HashtagFollow;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class HashtagFollowId implements Serializable
{
    private UUID user;
    private UUID hashtag;
}
