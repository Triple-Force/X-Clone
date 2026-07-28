package logic_core.app.usecase.tweet;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.UnlikeTweetRequest;
import logic_core.app.dto.response.LikeResponse;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.CurrentAuthContext;
import logic_core.app.security.SessionUserContext;
import logic_core.common.exception.*;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.tweetEvent.TweetUnlikedEvent;
import logic_core.domain.model.LikeRelation;
import logic_core.domain.model.TweetModel;
import logic_core.domain.model.UserModel;
import logic_core.domain.policy.InteractionPolicy;
import logic_core.domain.repository.RelationshipRepository;
import logic_core.domain.repository.TweetRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UnlikeTweetUseCase
{
    @NonNull private final InteractionPolicy interactionPolicy;
    @NonNull private final TweetRepository tweetRepository;
    @NonNull private final RelationshipRepository relationshipRepository;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<LikeResponse> execute(UnlikeTweetRequest request)
    {
        if (request == null || request.tweetId() == null)
        {
            return Result.failure("Tweet ID is required.");
        }

        try {
            SessionUserContext context =
                    lockOrchestrator.lockAndGetContextByToken(
                            request.sessionToken()
                    );

            UUID currentUserId = context.lockedUser().getId();

            TweetModel tweet = tweetRepository.findActiveByIdForUpdate(request.tweetId())
                    .orElseThrow(() -> new NotFoundException("Tweet not found or unavailable."));

            interactionPolicy.validateUnlike(currentUserId, tweet.getId(), tweet.getAuthorId());

            LikeRelation relation = LikeRelation.create(tweet.getId(), currentUserId);
            relationshipRepository.deleteLike(relation);

            TweetModel updatedTweet = tweet.toBuilder()
                    .likeCount(Math.max(0, tweet.getLikeCount() - 1))
                    .updatedAt(timeProvider.now())
                    .build();
            tweetRepository.update(updatedTweet);

            long currentLikeCount = updatedTweet.getLikeCount();

            eventPublisher.publish(new TweetUnlikedEvent(
                    tweet.getId(),
                    currentUserId,
                    timeProvider.now()
            ));

            return Result.success(new LikeResponse(currentUserId, tweet.getId(), false, currentLikeCount));
        }
        catch (AppException e)
        {
            return Result.failure(e.getMessage());
        }
        catch (Exception e)
        {

            return Result.failure("Failed to process unlike operation.");
        }
    }
}
