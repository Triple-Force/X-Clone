package logic_core.infrastructure.mapper;

import logic_core.domain.model.SessionModel;
import logic_core.infrastructure.persistence.entity.session.SessionEntity;
import logic_core.infrastructure.persistence.entity.UserEntity;

public final class SessionEntityMapper {
    private SessionEntityMapper() {}

    public static SessionModel toModel(SessionEntity entity) {
        if (entity == null) return null;
        return SessionModel.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .token(entity.getToken())
                .expiresAt(entity.getExpiresAt())
                .build();
    }

    public static SessionEntity toPersistence(SessionModel model, UserEntity user) {
        if (model == null) return null;
        return SessionEntity.builder()
                .user(user)
                .token(model.getToken())
                .expiresAt(model.getExpiresAt())
                .build();
    }
}
