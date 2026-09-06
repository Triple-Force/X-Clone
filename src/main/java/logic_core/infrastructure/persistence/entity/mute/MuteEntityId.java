package logic_core.infrastructure.persistence.entity.mute;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class MuteEntityId implements Serializable {
    private UUID muter;
    private UUID muted;
}
