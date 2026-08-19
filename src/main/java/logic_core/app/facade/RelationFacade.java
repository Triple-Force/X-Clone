package logic_core.app.facade;

import logic_core.app.dto.request.*;
import logic_core.app.dto.response.BlockActionResponse;
import logic_core.app.dto.response.FollowResponse;
import logic_core.app.dto.response.MuteResponse;
import logic_core.app.usecase.relation.*;
import logic_core.common.result.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RelationFacade
{
    @NonNull private final BlockUserUseCase blockUserUseCase;
    @NonNull private final FollowUserUseCase followUserUseCase;
    @NonNull private final MuteUserUseCase muteUserUseCase;
    @NonNull private final UnblockUserUseCase unblockUserUseCase;
    @NonNull private final UnfollowUserUseCase unfollowUserUseCase;
    @NonNull private final UnmuteUserUseCase unmuteUserUseCase;

    public Result<BlockActionResponse> block(BlockUserRequest request)
    {
        return blockUserUseCase.execute(request);
    }

    public Result<FollowResponse> follow(FollowUserRequest request)
    {
        return followUserUseCase.execute(request);
    }

    public Result<MuteResponse> mute(MuteUserRequest request)
    {
        return muteUserUseCase.execute(request);
    }

    public Result<BlockActionResponse> unblock(UnblockUserRequest request)
    {
        return unblockUserUseCase.execute(request);
    }

    public Result<FollowResponse> unfollow(UnfollowUserRequest request)
    {
        return unfollowUserUseCase.execute(request);
    }

    public Result<MuteResponse> unmute(UnmuteUserRequest request)
    {
        return unmuteUserUseCase.execute(request);
    }
}
