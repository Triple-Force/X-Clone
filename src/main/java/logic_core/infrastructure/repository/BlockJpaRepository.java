package logic_core.infrastructure.repository;

import logic_core.infrastructure.persistence.entity.block.BlockEntity;
import logic_core.infrastructure.persistence.entity.block.BlockEntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlockJpaRepository extends JpaRepository<BlockEntity, BlockEntityId> {

    /**
     * Find a specific block relation between two users.
     * 
     * Equivalent to BlockDao.findRelation():
     * WHERE b.blocker.id = :blockerId
     *   AND b.blocked.id = :blockedId
     * 
     * Note: Block has NO soft-delete filtering in the legacy implementation.
     */
    @Query("""
            SELECT b
            FROM BlockEntity b
            WHERE b.blocker.id = :blockerId
              AND b.blocked.id = :blockedId
            """)
    Optional<BlockEntity> findRelation(
            @Param("blockerId") UUID blockerId,
            @Param("blockedId") UUID blockedId
    );

    /**
     * Find all users blocked by the given blocker.
     * 
     * Equivalent to BlockDao.findByBlockerId():
     * WHERE b.blocker.id = :blockerId
     * 
     * Note: Block has NO soft-delete filtering in the legacy implementation.
     */
    @Query("""
            SELECT b
            FROM BlockEntity b
            WHERE b.blocker.id = :blockerId
            """)
    List<BlockEntity> findByBlockerId(@Param("blockerId") UUID blockerId);

    /**
     * Check if a block relation exists in either direction.
     * 
     * Equivalent to BlockDao.existsBlockRelation():
     * WHERE (b.blocker.id = :userA AND b.blocked.id = :userB)
     *    OR (b.blocker.id = :userB AND b.blocked.id = :userA)
     * 
     * Note: Block has NO soft-delete filtering in the legacy implementation.
     */
    @Query("""
            SELECT COUNT(b)
            FROM BlockEntity b
            WHERE (b.blocker.id = :userA AND b.blocked.id = :userB)
               OR (b.blocker.id = :userB AND b.blocked.id = :userA)
            """)
    long existsBlockRelation(
            @Param("userA") UUID userA,
            @Param("userB") UUID userB
    );
}
