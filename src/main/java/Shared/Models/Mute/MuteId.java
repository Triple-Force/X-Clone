package Shared.Models.Mute;

import Shared.Models.User.User;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class MuteId implements Serializable
{
    private User muter;
    private User muted;
}
