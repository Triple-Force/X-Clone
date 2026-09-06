package logic_core.infrastructure.repository;

import logic_core.domain.model.MuteRelation;
import logic_core.infrastructure.mapper.MuteEntityMapper;
import logic_core.infrastructure.persistence.entity.mute.MuteEntity;
import logic_core.infrastructure.persistence.entity.mute.MuteEntityId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure-level adapter for Mute persistence operations.
 * <p>
 * Uses {@link MuteJpaRepository} and {@link UserJpaRepository} for
 * Spring Data JPA operations, and {@link MuteEntityMapper} for
 * entity/domain mapping.
 * <p>
 * This adapter will be used by {@code RelationshipRepositoryAdapter} to
 * replace the legacy {@code MuteDao} dependency.
 */
@Component
@Transactional
public class MuteRepositoryAdapter {

    private final MuteJpaRepository muteJpaRepository;
    private final UserJpaRepository userJpaRepository;

    public MuteRepositoryAdapter(MuteJpaRepository muteJpaRepository,
                                  UserJpaRepository userJpaRepository) {
        this.muteJpaRepository = muteJpaRepository;
        this.userJpaRepository = userJpaRepository;
    }

    /**
     * Persists a new mute relation.
     *
     * @param mute the domain mute relation to persist
     */
    public void saveMute(MuteRelation mute) {
        var muter = userJpaRepository.getReferenceById(mute.getMuterId());
        var muted = userJpaRepository.getReferenceById(mute.getMutedId());
        MuteEntity entity = MuteEntityMapper.toPersistence(mute, muter, muted);
        muteJpaRepository.save(entity);
    }

    /**
     * Hard-deletes an existing mute relation.
     *
     * @param mute the domain mute relation to delete
     */
    public void deleteMute(MuteRelation mute) {
        MuteEntityId id = new MuteEntityId(
                mute.getMuterId(),
                mute.getMutedId()
        );
        muteJpaRepository.deleteById(id);
    }

    /**
     * Finds a specific mute relation between two users.
     * Filters out soft-deleted users.
     * 
     * Equivalent to MuteDao.findRelation() behavior.
     *
     * @param muterId  the muter's user ID
     * @param mutedId the muted user's ID
     * @return the mute relation if it exists and both users are active
     */
    public Optional<MuteRelation> findMuteRelation(UUID muterId, UUID mutedId) {
        return muteJpaRepository.findRelation(muterId, mutedId)
                .map(MuteEntityMapper::toDomain);
    }

    /**
     * Finds all users muted by the given muter.
     * Filters out soft-deleted users.
     * 
     * Equivalent to MuteDao.findByMuterId() behavior.
     *
     * @param muterId the muter's user ID
     * @return list of mute relations
     */
    public List<MuteRelation> findByMuterId(UUID muterId) {
        return muteJpaRepository.findByMuterId(muterId)
                .stream()
                .map(MuteEntityMapper::toDomain)
                .toList();
    }

    /**
     * Checks if muterId has muted mutedId.
     * Equivalent to findMuteRelation().isPresent().
     *
     * @param muterId  the potential muter
     * @param mutedId the potential muted
     * @return true if the mute relation exists
     */
    public boolean isMutedBy(UUID muterId, UUID mutedId) {
        return muteJpaRepository.findRelation(muterId, mutedId).isPresent();
    }

    /**
     * Checks if a mute relation exists in either direction.
     * Equivalent to isMutedBy(A, B) || isMutedBy(B, A).
     *
     * @param userId1 first user ID
     * @param userId2 second user ID
     * @return true if either user mutes the other
     */
    public boolean existsMuteRelation(UUID userId1, UUID userId2) {
        return isMutedBy(userId1, userId2) || isMutedBy(userId2, userId1);
    }
}
