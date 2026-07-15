package logic_core.app.mapper;

import logic_core.app.dto.response.UserSummaryResponse;
import logic_core.domain.model.UserModel;

public class UserSummaryResponseMapper
{
    public static UserSummaryResponse toResponse(UserModel model)
    {
        if (model == null)
            return null;

        return UserSummaryResponse.builder()
                .id(model.getId())
                .username(model.getUsername())
                .displayName(model.getDisplayName())
                .avatarUrl(model.getAvatarUrl())
                .verified(model.isVerified())
                .build();
    }
}