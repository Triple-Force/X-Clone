package logic_core.infrastructure.mapper;

import Shared.Models.Mute.Mute;
import Shared.Models.User.User;
import jakarta.persistence.EntityManager;
import logic_core.domain.model.MuteRelation;


public final class MutePersistenceMapper
{
    private MutePersistenceMapper()
    {
    }

    public static MuteRelation toDomain(Mute entity)
    {
        if (entity == null)
        {
            return null;
        }

        return MuteRelation.create(
                entity.getMuter().getId(),
                entity.getMuted().getId(),
                entity.getCreatedAt()
        );
    }

    public static Mute toPersistence(
            MuteRelation relation,
            EntityManager entityManager
    )
    {
        if (relation == null)
        {
            return null;
        }


        User muter = entityManager.getReference(
                User.class,
                relation.getMuterId()
        );
        User muted = entityManager.getReference(
                User.class,
                relation.getMutedId()
        );

        return Mute.builder()
                .muter(muter)
                .muted(muted)
                .createdAt(relation.getCreatedAt())
                .build();
    }
}