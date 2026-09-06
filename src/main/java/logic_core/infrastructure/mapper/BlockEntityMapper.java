package logic_core.infrastructure.mapper;

import logic_core.domain.model.BlockRelation;
import logic_core.infrastructure.persistence.entity.block.BlockEntity;
import logic_core.infrastructure.persistence.entity.UserEntity;

public final class BlockEntityMapper {

    private BlockEntityMapper() {
    }

    /**
     * Maps a BlockEntity to the domain BlockRelation model.
     * 
     * Extracts blocker/blocked UUIDs from the entity's UserEntity references
     * and preserves the createdAt timestamp.
     */
    public static BlockRelation toDomain(BlockEntity entity) {
        if (entity == null) {
            return null;
        }

        return BlockRelation.create(
                entity.getBlocker().getId(),
                entity.getBlocked().getId(),
                entity.getCreatedAt()
        );
    }

    /**
     * Maps a BlockRelation domain model to a new BlockEntity.
     * 
     * The caller must provide resolved UserEntity references for blocker and blocked.
     * Use UserJpaRepository.getReferenceById() to obtain lazy proxies.
     */
    public static BlockEntity toPersistence(
            BlockRelation relation,
            UserEntity blocker,
            UserEntity blocked) {
        if (relation == null) {
            return null;
        }

        BlockEntity entity = new BlockEntity();
        entity.setBlocker(blocker);
        entity.setBlocked(blocked);
        entity.setCreatedAt(relation.getCreatedAt());
        return entity;
    }
}
