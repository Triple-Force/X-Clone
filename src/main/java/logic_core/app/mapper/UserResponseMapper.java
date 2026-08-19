package logic_core.app.mapper;

import logic_core.app.dto.response.UserResponse;
import logic_core.domain.model.UserModel;

public class UserResponseMapper
{
    public static UserResponse toResponse(UserModel model)
    {
        if (model == null)
            return null;

        return UserResponse.builder()
                .id(model.getId())
                .username(model.getUsername())
                .email(model.getEmail())
                .displayName(model.getDisplayName())
                .bio(model.getBio())
                .avatarUrl(model.getAvatarUrl())
                .bannerUrl(model.getBannerUrl())
                .verified(model.isVerified())
                .active(model.isActive())
                .createdAt(model.getCreatedAt())
                .build();
    }
}
