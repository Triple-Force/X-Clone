package logic_core.app.mapper;

import logic_core.app.dto.response.UserPublicResponse;
import logic_core.domain.model.UserModel;

public class UserPublicResponseMapper
{
    public static UserPublicResponse toResponse(UserModel model)
    {
        if (model == null)
            return null;

        return UserPublicResponse.builder()
                .id(model.getId())
                .username(model.getUsername())
                .displayName(model.getDisplayName())
                .bio(model.getBio())
                .avatarUrl(model.getAvatarUrl())
                .bannerUrl(model.getBannerUrl())
                .verified(model.isVerified())
                .build();
    }
}
