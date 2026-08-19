package logic_core.infrastructure.dao;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Block.Block;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class BlockDao extends GenericDAO<Block>
{
    public BlockDao()
    {
        super(Block.class);
    }

    public void insert(Block block)
    {
        super.insert(block);
    }

    public void delete(Block block)
    {
        super.hardDelete(block);
    }

    public Optional<Block> findRelation(UUID blockerId, UUID blockedId)
    {
        return Optional.ofNullable(findOneByJpql(
                "SELECT b FROM Block b " +
                        "WHERE b.blocker.id = :blockerId " +
                        "AND b.blocked.id = :blockedId",
                query -> query
                        .setParameter("blockerId", blockerId)
                        .setParameter("blockedId", blockedId)
        ));
    }

    public List<Block> findByBlockerId(UUID blockerId)
    {
        return findByJpql(
                "SELECT b FROM Block b WHERE b.blocker.id = :blockerId",
                query -> query.setParameter("blockerId", blockerId)
        );
    }

    public boolean existsBlockRelation(UUID userA, UUID userB)
    {
        long count = countByJpql(
                "SELECT COUNT(b) FROM Block b " +
                        "WHERE (b.blocker.id = :userA AND b.blocked.id = :userB) " +
                        "OR (b.blocker.id = :userB AND b.blocked.id = :userA)",
                query -> query
                        .setParameter("userA", userA)
                        .setParameter("userB", userB)
        );

        return count > 0;
    }
}
