package logic_core.infrastructure.mapper;

import Shared.Models.Block.Block;
import Shared.Models.User.User;
import jakarta.persistence.EntityManager;
import logic_core.domain.model.BlockRelation;


public final class BlockPersistenceMapper
{
    private BlockPersistenceMapper()
    {
    }

    public static BlockRelation toDomain(Block entity)
    {
        if (entity == null)
        {
            return null;
        }

        return BlockRelation.create(
                entity.getBlocker().getId(),
                entity.getBlocked().getId(),
                entity.getCreatedAt()
        );
    }

    public static Block toPersistence(
            BlockRelation relation,
            EntityManager entityManager
    )
    {
        if (relation == null)
        {
            return null;
        }


        User blockerRef = entityManager.getReference(
                User.class,
                relation.getBlockerId()
        );
        User blockedRef = entityManager.getReference(
                User.class,
                relation.getBlockedId()
        );

        return Block.builder()
                .blocker(blockerRef)
                .blocked(blockedRef)
                .createdAt(relation.getCreatedAt())
                .build();
    }
}
