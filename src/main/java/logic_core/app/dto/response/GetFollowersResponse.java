package logic_core.app.dto.response;

import java.util.List;
import java.util.UUID;

public record GetFollowersResponse(
        UUID userId,
        List<UserSummaryResponse> followers
) {}