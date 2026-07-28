package logic_core.app.dto.request;

import java.util.UUID;

public record GetFollowersRequest(
        UUID userId,
        int limit,
        int offset
) {}