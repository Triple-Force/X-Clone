package logic_core.common.security;

import java.util.UUID;

public record AuthPrincipal(
        UUID userId,
        String username,
        UUID sessionId,
        String sessionToken
) {}
