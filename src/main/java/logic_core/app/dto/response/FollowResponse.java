package logic_core.app.dto.response;

public record FollowResponse(
        boolean following,
        long followersCount
) {}