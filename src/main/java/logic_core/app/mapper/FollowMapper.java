package logic_core.app.mapper;

import logic_core.app.dto.response.FollowResponse;
import logic_core.domain.model.FollowRelation;

public final class FollowMapper
{
    private FollowMapper()
    {

    }

    public static FollowResponse toResponse(boolean following, long followersCount)
    {
        return new FollowResponse(
                following,
                followersCount
        );
    }

    public static FollowResponse fromRelation(FollowRelation relation, long followersCount)
    {
        return new FollowResponse(
                relation != null,
                followersCount
        );
    }
}
