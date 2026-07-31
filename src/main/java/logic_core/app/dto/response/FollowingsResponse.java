package logic_core.app.dto.response;

import java.util.List;

public record FollowingsResponse(
        List<UserSummaryResponse> users
) {}