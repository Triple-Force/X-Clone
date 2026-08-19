package logic_core.app.dto.response;

import java.util.UUID;

public record UserSearchResponse(
        UUID id,
        String username,
        String displayName,
        String avatarUrl
) {}