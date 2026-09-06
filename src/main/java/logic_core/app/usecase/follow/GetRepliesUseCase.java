package logic_core.app.usecase.follow;

import logic_core.app.dto.request.GetRepliesRequest;
import logic_core.app.dto.timeline.TimelineTweet;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.SessionUserContext;
import logic_core.common.exception.AppException;
import logic_core.common.result.Result;
import logic_core.domain.repository.TweetRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Returns the direct replies of a tweet in the same {@link TimelineTweet} shape
 * the timeline transport uses (author info + interaction counts, oldest first).
 *
 * <p>The actor is derived from the authenticated session token; replies whose
 * author is blocked either way are excluded (same block semantics as the
 * timeline queries). Reply rows are soft-delete filtered by the repository.
 */
@Service
@RequiredArgsConstructor
public class GetRepliesUseCase
{
    @NonNull private final TweetRepository tweetRepository;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    public Result<List<TimelineTweet>> execute(GetRepliesRequest request)
    {
        if (request == null || request.tweetId() == null)
        {
            return Result.failure("Tweet ID is required.");
        }

        try
        {
            SessionUserContext context = lockOrchestrator.lockAndGetContextByToken(request.token());

            UUID actorId = context.lockedUser().getId();

            List<TimelineTweet> replies = tweetRepository.getRepliesOfTweet(actorId, request.tweetId());

            return Result.success(replies);
        }
        catch (AppException e)
        {
            return Result.failure(e.getMessage());
        }
        catch (Exception e)
        {
            return Result.failure("Failed to load replies.");
        }
    }
}
