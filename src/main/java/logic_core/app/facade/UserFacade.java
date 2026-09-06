package logic_core.app.facade;

import logic_core.app.dto.request.*;
import logic_core.app.dto.response.*;
import logic_core.app.usecase.User.*;
import logic_core.common.result.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserFacade
{
    private final DeleteAccountUseCase deleteAccountUseCase;
    private final GetProfileUseCase getProfileUseCase;
    private final SearchUsersUseCase searchUsersUseCase;
    private final UpdateAvatarUseCase updateAvatarUseCase;
    private final UpdateBannerUseCase updateBannerUseCase;
    private final UpdateBioUseCase updateBioUseCase;
    private final UpdateEmailUseCase updateEmailUseCase;
    private final UpdatePasswordUseCase updatePasswordUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final UpdateCompleteProfileUseCase updateCompleteProfileUseCase;
    private final GetIsFollowingUseCase getIsFollowingUseCase;
    private final GetIsLikedUseCase getIsLikedUseCase;


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
