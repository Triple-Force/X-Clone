package logic_core.domain.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BlockRelation
{
    private UUID blockerId;
    private UUID blockedId;
    private OffsetDateTime createdAt;

    public static BlockRelation create(UUID blockerId, UUID blockedId, OffsetDateTime time)
    {

        Objects.requireNonNull(blockerId, "Block.blocker cannot be null");
        Objects.requireNonNull(blockedId, "Block.blocked cannot be null");
        Objects.requireNonNull(time, "block.createdAt cannot be null.");

        if (blockerId.equals(blockedId))
        {
            throw new IllegalArgumentException("Cannot block yourself.");
        }

        return BlockRelation.builder()
                .blockerId(blockerId)
                .blockedId(blockedId)
                .createdAt(time)
                .build();
    }
}

