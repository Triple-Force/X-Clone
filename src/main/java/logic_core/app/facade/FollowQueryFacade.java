package logic_core.app.facade;

import logic_core.app.dto.request.GetFollowersRequest;
import logic_core.app.dto.request.GetFollowingsRequest;
import logic_core.app.dto.response.FollowingsResponse;
import logic_core.app.dto.response.GetFollowersResponse;
import logic_core.app.usecase.follow.GetFollowersUseCase;
import logic_core.app.usecase.follow.GetFollowingsUseCase;
import logic_core.common.result.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FollowQueryFacade
{
    @NonNull private final GetFollowingsUseCase getFollowingsUseCase;
    @NonNull private final GetFollowersUseCase getFollowersUseCase;

    public Result<FollowingsResponse> getFollowings(GetFollowingsRequest request)
    {
        return getFollowingsUseCase.execute(request);
    }

    public Result<GetFollowersResponse> getFollowers(GetFollowersRequest request)
    {
        return getFollowersUseCase.execute(request);
    }
}
