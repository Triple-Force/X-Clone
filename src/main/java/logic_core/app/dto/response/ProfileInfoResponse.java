package logic_core.app.dto.response;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProfileInfoResponse(
        UUID userId,
        String username,
        String displayName,
        String bio,
        String avatarUrl,
        String banner,
        long followers,
        long following,
        long tweets,
        boolean verified,
        OffsetDateTime joinedAt
) {}