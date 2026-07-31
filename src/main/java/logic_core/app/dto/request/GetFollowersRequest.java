package logic_core.app.dto.request;

import java.util.UUID;

public record GetFollowersRequest(
        String sessionToken,
        UUID targetId
//        int limit,
//        int offset
) {}