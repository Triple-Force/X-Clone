package logic_core.app.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserSummaryResponse(
        UUID id,
        String username,
        String displayName,
        String avatarUrl,
        boolean verified
) {}