package Shared.Models.Like;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class LikeId implements Serializable
{
    private UUID user;
    private UUID tweet;
}
