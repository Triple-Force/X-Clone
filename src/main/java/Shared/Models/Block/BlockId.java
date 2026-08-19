package Shared.Models.Block;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class BlockId implements Serializable
{
    private UUID blocker;
    private UUID blocked;
}
