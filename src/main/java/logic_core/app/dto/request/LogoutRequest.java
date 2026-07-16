package logic_core.app.dto.request;

import java.util.UUID;

public record LogoutRequest(
        String sessionToken,
        UUID userId
) {}