package logic_core.infrastructure.mapper;

import logic_core.domain.model.MuteRelation;
import logic_core.infrastructure.persistence.entity.mute.MuteEntity;
import logic_core.infrastructure.persistence.entity.UserEntity;

public final class MuteEntityMapper {

    private MuteEntityMapper() {
    }

    /**
     * Maps a MuteEntity to the domain MuteRelation model.
     * 
     * Extracts muter/muted UUIDs from the entity's UserEntity references
     * and preserves the createdAt timestamp.
     */
    public static MuteRelation toDomain(MuteEntity entity) {
        if (entity == null) {
            return null;
        }

        return MuteRelation.create(
                entity.getMuter().getId(),
                entity.getMuted().getId(),
                entity.getCreatedAt()
        );
    }

    /**
     * Maps a MuteRelation domain model to a new MuteEntity.
     * 
     * The caller must provide resolved UserEntity references for muter and muted.
     * Use UserJpaRepository.getReferenceById() to obtain lazy proxies.
     */
    public static MuteEntity toPersistence(
            MuteRelation relation,
            UserEntity muter,
            UserEntity muted) {
        if (relation == null) {
            return null;
        }

        MuteEntity entity = new MuteEntity();
        entity.setMuter(muter);
        entity.setMuted(muted);
        entity.setCreatedAt(relation.getCreatedAt());
        return entity;
    }
}
