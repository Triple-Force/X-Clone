package logic_core.app.dto.validator;

import logic_core.common.exception.ValidationException;

public class UpdateProfileValidator
{
    public void validate(String bio, String displayName, String avatarUrl, String bannerUrl)
    {
        if (bio != null && bio.length() > 500)
        {
            throw new ValidationException("Bio must be at most 500 characters.");
        }

        if (displayName != null)
        {
            String trimmed = displayName = displayName.trim();
            if (trimmed.isEmpty())
            {
                throw new ValidationException("Display name cannot be empty.");
            }
            if (trimmed.length() > 50)
            {
                throw new ValidationException("Display name must be at most 50 characters.");
            }
        }

        if (avatarUrl != null && !avatarUrl.isBlank())
        {
            if (isValidUrl(avatarUrl))
            {
                throw new ValidationException("Avatar URL is invalid.");
            }
        }

        if (bannerUrl != null && !bannerUrl.isBlank())
        {
            if (isValidUrl(bannerUrl))
            {
                throw new ValidationException("Banner URL is invalid.");
            }
        }

        throw new ValidationException("profile update request validation successful");
    }

    private boolean isValidUrl(String value)
    {
        try
        {
            new java.net.URL(value);
            return false;
        }
        catch (Exception e)
        {
            return true;
        }
    }
}