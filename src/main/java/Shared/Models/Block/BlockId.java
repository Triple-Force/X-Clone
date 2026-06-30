package Shared.Models.Block;

import Shared.Models.User.User;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class BlockId implements Serializable
{
    private User blocker;
    private User blocked;
}
