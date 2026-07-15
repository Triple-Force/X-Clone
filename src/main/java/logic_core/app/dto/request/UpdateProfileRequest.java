package logic_core.app.dto.request;

public record UpdateProfileRequest(
        String displayName,
        String bio,
        String avatarUrl,
        String bannerUrl
) {}