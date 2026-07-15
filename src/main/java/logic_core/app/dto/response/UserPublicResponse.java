package logic_core.app.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserPublicResponse(
        UUID id,
        String username,
        String displayName,
        String bio,
        String avatarUrl,
        String bannerUrl,
        boolean verified
) {}
