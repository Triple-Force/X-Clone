package logic_core.infrastructure.persistence.entity.block;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class BlockEntityId implements Serializable {
    private UUID blocker;
    private UUID blocked;
}
