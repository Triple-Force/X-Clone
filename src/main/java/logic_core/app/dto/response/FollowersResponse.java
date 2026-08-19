package logic_core.app.dto.response;

import java.util.List;
import java.util.UUID;

public record FollowersResponse(
        UUID userId,
        List<UserSummaryResponse> followers,
        int limit,
        int offset,
        boolean hasMore
) {}