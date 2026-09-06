package logic_core.infrastructure.persistence.entity.like;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class LikeEntityId implements Serializable {
    private UUID user;
    private UUID tweet;
}
