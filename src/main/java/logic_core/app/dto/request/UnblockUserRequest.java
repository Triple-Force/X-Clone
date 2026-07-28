package logic_core.app.dto.request;

import java.util.UUID;

public record UnblockUserRequest(
        UUID unblockedId,
        String sessionToken
) {}