package logic_core.infrastructure.mapper;

import Shared.Models.User.User;
import logic_core.domain.model.UserModel;

public final class UserPersistenceMapper
{
    private UserPersistenceMapper()
    {
    }

    public static UserModel toModel(User entity)
    {
        if (entity == null)
        {
            return null;
        }

        return UserModel.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .displayName(entity.getDisplayName())
                .bio(entity.getBio())
                .avatarUrl(entity.getAvatarUrl())
                .bannerUrl(entity.getBannerUrl())
                .verified(entity.isVerified())
                .active(entity.isActive())
                .deleted(entity.isDeleted())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static User toPersistence(UserModel model)
    {
        if (model == null)
        {
            return null;
        }

        User entity = new User();
        updateEntity(entity, model);
        return entity;
    }

    public static void updateEntity(User entity, UserModel model)
    {
        if (entity == null || model == null)
        {
            return;
        }

        entity.setUsername(model.getUsername());
        entity.setEmail(model.getEmail());
        entity.setPasswordHash(model.getPasswordHash());
        entity.setDisplayName(model.getDisplayName());
        entity.setBio(model.getBio());
        entity.setAvatarUrl(model.getAvatarUrl());
        entity.setBannerUrl(model.getBannerUrl());
        entity.setVerified(model.isVerified());
        entity.setActive(model.isActive());
        entity.setDeleted(model.isDeleted());
    }
}