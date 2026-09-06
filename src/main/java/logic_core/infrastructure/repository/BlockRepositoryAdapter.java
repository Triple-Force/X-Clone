package logic_core.infrastructure.repository;

import logic_core.domain.model.BlockRelation;
import logic_core.infrastructure.mapper.BlockEntityMapper;
import logic_core.infrastructure.persistence.entity.block.BlockEntity;
import logic_core.infrastructure.persistence.entity.block.BlockEntityId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure-level adapter for Block persistence operations.
 * <p>
 * Uses {@link BlockJpaRepository} and {@link UserJpaRepository} for
 * Spring Data JPA operations, and {@link BlockEntityMapper} for
 * entity/domain mapping.
 * <p>
 * This adapter will be used by {@code RelationshipRepositoryAdapter} to
 * replace the legacy {@code BlockDao} dependency.
 */
@Component
@Transactional
public class BlockRepositoryAdapter {

    private final BlockJpaRepository blockJpaRepository;
    private final UserJpaRepository userJpaRepository;

    public BlockRepositoryAdapter(BlockJpaRepository blockJpaRepository,
                                   UserJpaRepository userJpaRepository) {
        this.blockJpaRepository = blockJpaRepository;
        this.userJpaRepository = userJpaRepository;
    }

    /**
     * Persists a new block relation.
     *
     * @param block the domain block relation to persist
     */
    public void saveBlock(BlockRelation block) {
        var blocker = userJpaRepository.getReferenceById(block.getBlockerId());
        var blocked = userJpaRepository.getReferenceById(block.getBlockedId());
        BlockEntity entity = BlockEntityMapper.toPersistence(block, blocker, blocked);
        blockJpaRepository.save(entity);
    }

    /**
     * Hard-deletes an existing block relation.
     *
     * @param block the domain block relation to delete
     */
    public void deleteBlock(BlockRelation block) {
        BlockEntityId id = new BlockEntityId(
                block.getBlockerId(),
                block.getBlockedId()
        );
        blockJpaRepository.deleteById(id);
    }

    /**
     * Finds a specific block relation between two users.
     * 
     * Note: Block has NO soft-delete filtering in the legacy implementation.
     *
     * @param blockerId  the blocker's user ID
     * @param blockedId the blocked user's ID
     * @return the block relation if it exists
     */
    public Optional<BlockRelation> findBlockRelation(UUID blockerId, UUID blockedId) {
        return blockJpaRepository.findRelation(blockerId, blockedId)
                .map(BlockEntityMapper::toDomain);
    }

    /**
     * Finds all users blocked by the given blocker.
     * 
     * Note: Block has NO soft-delete filtering in the legacy implementation.
     *
     * @param blockerId the blocker's user ID
     * @return list of block relations
     */
    public List<BlockRelation> findByBlockerId(UUID blockerId) {
        return blockJpaRepository.findByBlockerId(blockerId)
                .stream()
                .map(BlockEntityMapper::toDomain)
                .toList();
    }

    /**
     * Checks if blockerId has blocked blockedId.
     * Equivalent to findBlockRelation().isPresent().
     *
     * @param blockerId  the potential blocker
     * @param blockedId the potential blocked
     * @return true if the block relation exists
     */
    public boolean isBlockedBy(UUID blockerId, UUID blockedId) {
        return blockJpaRepository.findRelation(blockerId, blockedId).isPresent();
    }

    /**
     * Checks if a block relation exists in either direction.
     * Equivalent to isBlockedBy(A, B) || isBlockedBy(B, A).
     *
     * @param userId1 first user ID
     * @param userId2 second user ID
     * @return true if either user blocks the other
     */
    public boolean existsBlockRelation(UUID userId1, UUID userId2) {
        return blockJpaRepository.existsBlockRelation(userId1, userId2) > 0;
    }
}
