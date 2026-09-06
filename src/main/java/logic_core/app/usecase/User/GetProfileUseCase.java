package logic_core.app.usecase.User;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.GetProfileRequest;
import logic_core.app.dto.response.ProfileInfoResponse;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.common.exception.NotFoundException;
import logic_core.common.result.Result;
import logic_core.domain.model.UserModel;
import logic_core.domain.repository.RelationshipRepository;
import logic_core.domain.repository.TweetRepository;
import logic_core.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetProfileUseCase
{
    @NonNull private final UserRepository repository;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;
    @NonNull private final RelationshipRepository followRepository;
    @NonNull private final TweetRepository tweetRepository;

    @Transactional
    public Result<ProfileInfoResponse> execute(GetProfileRequest request)
    {
        try
        {
            lockOrchestrator.lockAndGetContextByToken(request.sessionToken());

            UserModel user = repository.findById(request.userId())
                    .orElseThrow(()-> new NotFoundException("user not found"));


            long followers = followRepository.countFollowers(user.getId());
            long following = followRepository.countFollowing(user.getId());
            long tweets = tweetRepository.countTweetsById(user.getId());


            return Result.success(new ProfileInfoResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getDisplayName(),
                    user.getBio(),
                    user.getAvatarUrl(),
                    user.getBannerUrl(),
                    followers,
                    following,
                    tweets,
                    user.isVerified(),
                    user.getCreatedAt())
            );
        }
        catch (Exception e)
        {
            return Result.failure(e.getMessage());
        }
    }
}