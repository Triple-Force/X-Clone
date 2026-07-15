package logic_core.app.mapper;

import logic_core.app.dto.response.BlockActionResponse;
import logic_core.app.dto.response.BlockStatusResponse;
import logic_core.domain.model.BlockRelation;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public final class BlockMapper
{
    private BlockMapper()
    {
    }

    public static BlockStatusResponse toStatusResponse(boolean blocked)
    {
        return new BlockStatusResponse(blocked);
    }

    public static BlockStatusResponse toStatusResponse(BlockRelation relation)
    {
        return new BlockStatusResponse(relation != null);
    }

    public static BlockActionResponse toBlockedResponse(BlockRelation relation)
    {
        Objects.requireNonNull(relation, "BlockRelation cannot be null when creating blocked response.");

        return BlockActionResponse.builder()
                .success(true)
                .blocked(true)
                .blockerId(relation.getBlockerId())
                .blockedId(relation.getBlockedId())
                .occurredAt(relation.getCreatedAt())
                .build();
    }

    public static BlockActionResponse toUnblockedResponse(UUID blockerId, UUID blockedId)
    {
        return BlockActionResponse.builder()
                .success(true)
                .blocked(false)
                .blockerId(blockerId)
                .blockedId(blockedId)
                .occurredAt(OffsetDateTime.now())
                .build();
    }
}
