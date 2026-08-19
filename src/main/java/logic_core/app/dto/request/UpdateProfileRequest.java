package logic_core.app.dto.request;

import java.util.UUID;

public record UpdateProfileRequest(
        String sessionToken,
        UUID userId,
        String displayName,
        String username
) {}