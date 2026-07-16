package logic_core.app.dto.response;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record UserResponse(
        UUID id,
        String username,
        String email,
        String displayName,
        String bio,
        String avatarUrl,
        String bannerUrl,
        boolean verified,
        boolean active,
        OffsetDateTime createdAt
) {}