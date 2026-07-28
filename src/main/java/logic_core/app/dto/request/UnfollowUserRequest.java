package logic_core.app.dto.request;

import java.util.UUID;

public record UnfollowUserRequest(
        UUID unfollowedId,
        String sessionToken
) {}
