package logic_core.app.usecase.tweet;

import Shared.Models.Like.Like;
import jakarta.transaction.Transactional;
import logic_core.app.dto.request.LikeTweetRequest;
import logic_core.app.dto.response.LikeResponse;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.CurrentAuthContext;
import logic_core.app.security.SessionUserContext;
import logic_core.common.exception.*;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.tweetEvent.TweetLikedEvent;
import logic_core.domain.model.LikeRelation;
import logic_core.domain.model.TweetModel;
import logic_core.domain.policy.InteractionPolicy;
import logic_core.domain.repository.RelationshipRepository;
import logic_core.domain.repository.TweetRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class LikeTweetUseCase
{
    @NonNull private final InteractionPolicy interactionPolicy;
    @NonNull private final TweetRepository tweetRepository;
    @NonNull private final RelationshipRepository relationshipRepository;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<LikeResponse> execute(LikeTweetRequest request)
    {
        if (request == null || request.tweetId() == null)
        {
            return Result.failure("Tweet ID is required.");
        }

        try
        {
            SessionUserContext context =
                    lockOrchestrator.lockAndGetContextByToken(
                            request.sessionToken()
                    );

            UUID currentUserId = context.lockedUser().getId();

            TweetModel tweet = tweetRepository.findActiveByIdForUpdate(request.tweetId())
                    .orElseThrow(() -> new NotFoundException("Tweet not found or unavailable."));

            interactionPolicy.validateLike(currentUserId, tweet.getId(), tweet.getAuthorId());

            if (relationshipRepository.existsLikeRelation(currentUserId, tweet.getId()))
            {
                relationshipRepository.deleteLike(LikeRelation.create(tweet.getId(), currentUserId));

                long count = relationshipRepository.countLikesByTweetId(tweet.getId());

                System.out.println("likes : " + count);
                return Result.success(
                        new LikeResponse(
                                currentUserId,
                                tweet.getId(),
                                false,
                                count
                        )
                );
            }
            else
            {
                relationshipRepository.saveLike(LikeRelation.create(tweet.getId(), currentUserId));

                long totalLikesCount = relationshipRepository.countLikesByTweetId(tweet.getId());

                eventPublisher.publish(new TweetLikedEvent(tweet.getId(), currentUserId, timeProvider.now()));

                return Result.success(new LikeResponse(currentUserId, tweet.getId(), true, totalLikesCount));
            }

        }
        catch (ValidationException | ForbiddenException | ConflictException | NotFoundException e)
        {
            return Result.failure(e.getMessage());
        }
        catch (Exception e)
        {
            return Result.failure("Unexpected error during like: " + e.getMessage());
        }
    }
}
