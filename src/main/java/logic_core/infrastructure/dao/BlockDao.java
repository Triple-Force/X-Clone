package logic_core.infrastructure.dao;

import Shared.Models.Block.Block;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class BlockDao extends AbstractJpaDao<Block>
{
    public BlockDao(EntityManager entityManager)
    {
        super(entityManager, Block.class);
    }

    public void insert(Block block)
    {
        Objects.requireNonNull(block, "block must not be null");
        persist(block);
    }

    public void delete(Block block)
    {
        Objects.requireNonNull(block, "block must not be null");
        remove(block);
    }

    public Optional<Block> findRelation(UUID blockerId, UUID blockedId)
    {
        Objects.requireNonNull(blockerId, "blockerId must not be null");
        Objects.requireNonNull(blockedId, "blockedId must not be null");

        List<Block> blocks = entityManager.createQuery(
                        "SELECT b FROM Block b " +
                                "WHERE b.blocker.id = :blockerId " +
                                "AND b.blocked.id = :blockedId",
                        Block.class
                )
                .setParameter("blockerId", blockerId)
                .setParameter("blockedId", blockedId)
                .setMaxResults(1)
                .getResultList();

        return blocks.stream().findFirst();
    }

    public List<Block> findByBlockerId(UUID blockerId)
    {
        Objects.requireNonNull(blockerId, "blockerId must not be null");

        return entityManager.createQuery(
                        "SELECT b FROM Block b WHERE b.blocker.id = :blockerId",
                        Block.class
                )
                .setParameter("blockerId", blockerId)
                .getResultList();
    }

    public boolean existsBlockRelation(UUID userA, UUID userB)
    {
        Objects.requireNonNull(userA, "userA must not be null");
        Objects.requireNonNull(userB, "userB must not be null");

        Long count = entityManager.createQuery(
                        "SELECT COUNT(b) FROM Block b " +
                                "WHERE (b.blocker.id = :userA AND b.blocked.id = :userB) " +
                                "OR (b.blocker.id = :userB AND b.blocked.id = :userA)",
                        Long.class
                )
                .setParameter("userA", userA)
                .setParameter("userB", userB)
                .getSingleResult();

        return count > 0;
    }
}
