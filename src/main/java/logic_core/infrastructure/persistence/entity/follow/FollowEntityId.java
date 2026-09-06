package logic_core.infrastructure.persistence.entity.follow;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class FollowEntityId implements Serializable {
    private UUID follower;
    private UUID following;
}
