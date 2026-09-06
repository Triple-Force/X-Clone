package logic_core.infrastructure.repository;

import logic_core.infrastructure.persistence.entity.mute.MuteEntity;
import logic_core.infrastructure.persistence.entity.mute.MuteEntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MuteJpaRepository extends JpaRepository<MuteEntity, MuteEntityId> {

    /**
     * Find a specific mute relation between two users.
     * 
     * Equivalent to MuteDao.findRelation():
     * WHERE m.muter.id = :muterId
     *   AND m.muted.id = :mutedId
     *   AND m.muter.isDeleted = false
     *   AND m.muted.isDeleted = false
     * 
     * Note: Mute has soft-delete filtering for both muter and muted users.
     */
    @Query("""
            SELECT m
            FROM MuteEntity m
            WHERE m.muter.id = :muterId
              AND m.muted.id = :mutedId
              AND m.muter.isDeleted = false
              AND m.muted.isDeleted = false
            """)
    Optional<MuteEntity> findRelation(
            @Param("muterId") UUID muterId,
            @Param("mutedId") UUID mutedId
    );

    /**
     * Find all users muted by the given muter.
     * 
     * Equivalent to MuteDao.findByMuterId():
     * WHERE m.muter.id = :muterId
     *   AND m.muter.isDeleted = false
     *   AND m.muted.isDeleted = false
     * 
     * Note: Mute has soft-delete filtering for both muter and muted users.
     */
    @Query("""
            SELECT m
            FROM MuteEntity m
            WHERE m.muter.id = :muterId
              AND m.muter.isDeleted = false
              AND m.muted.isDeleted = false
            """)
    List<MuteEntity> findByMuterId(@Param("muterId") UUID muterId);
}
