package logic_core.app.facade;

import logic_core.app.dto.request.*;
import logic_core.app.dto.response.*;
import logic_core.app.usecase.User.*;
import logic_core.common.result.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class UserFacade
{
    @NonNull private final DeleteAccountUseCase deleteAccountUseCase;
    @NonNull private final GetProfileUseCase getProfileUseCase;
    @NonNull private final SearchUsersUseCase searchUsersUseCase;
    @NonNull private final UpdateAvatarUseCase updateAvatarUseCase;
    @NonNull private final UpdateBannerUseCase updateBannerUseCase;
    @NonNull private final UpdateBioUseCase updateBioUseCase;
    @NonNull private final UpdateEmailUseCase updateEmailUseCase;
    @NonNull private final UpdatePasswordUseCase updatePasswordUseCase;
    @NonNull private final UpdateProfileUseCase updateProfileUseCase;
    @NonNull private final UpdateCompleteProfileUseCase updateCompleteProfileUseCase;
    @NonNull private final GetIsFollowingUseCase getIsFollowingUseCase;
    @NonNull private final GetIsLikedUseCase getIsLikedUseCase;


    public Result<Void> deleteAccount(DeleteAccountRequest request)
    {
        return deleteAccountUseCase.execute(request);
    }

    public Result<ProfileInfoResponse> getProfile(GetProfileRequest request)
    {
        return getProfileUseCase.execute(request);
    }

    public Result<List<UserSearchResponse>> searchUsers(SearchUsersRequest request)
    {
        return searchUsersUseCase.execute(request);
    }

    public Result<UpdateAvatarResponse> updateAvatar(UpdateAvatarRequest request)
    {
        return updateAvatarUseCase.execute(request);
    }

    public Result<UpdateBannerResponse> updateBanner(UpdateBannerRequest request)
    {
        return updateBannerUseCase.execute(request);
    }

    public  Result<UpdateBioResponse> updateBio(UpdateBioRequest request)
    {
        return updateBioUseCase.execute(request);
    }

    public Result<Void> updateEmail(UpdateEmailRequest request)
    {
        return updateEmailUseCase.execute(request);
    }

    public Result<Void> updatePassword(UpdatePasswordRequest request)
    {
        return updatePasswordUseCase.execute(request);
    }

    public Result<Void> updateProfile(UpdateProfileRequest request)
    {
        return updateProfileUseCase.execute(request);
    }

    public Result<UpdateCompleteProfileResponse> updateCompleteProfile(UpdateCompleteProfileRequest request)
    {
        return updateCompleteProfileUseCase.execute(request);
    }

    public Result<GetIsFollowingResponse> isFollow(GetIsFollowingRequest request)
    {
        return getIsFollowingUseCase.execute(request);
    }

    public Result<GetIsLikedResponse> isLiked(GetIsLikedRequest request)
    {
        return getIsLikedUseCase.execute(request);
    }
}
