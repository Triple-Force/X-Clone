package logic_core.app.dto.response;

public record UpdateProfileResponse(
        String bio,
        String avatarUrl,
        String bannerUrl,
        String displayName
) {}