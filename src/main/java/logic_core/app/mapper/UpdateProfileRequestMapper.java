package logic_core.app.mapper;

import logic_core.app.dto.request.UpdateProfileRequest;
import logic_core.domain.model.UserModel;

public class UpdateProfileRequestMapper
{
    public static void apply(UserModel model, String displayName, String bio, String avatarUrl, String bannerUrl)
    {

        if (displayName != null)
            model.setDisplayName(displayName);

        if (bio != null)
            model.setBio(bio);

        if (avatarUrl != null)
            model.setAvatarUrl(avatarUrl);

        if (bannerUrl != null)
            model.setBannerUrl(bannerUrl);
    }
}