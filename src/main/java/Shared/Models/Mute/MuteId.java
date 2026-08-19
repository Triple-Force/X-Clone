package Shared.Models.Mute;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class MuteId implements Serializable
{
    private UUID muter;
    private UUID muted;
}
