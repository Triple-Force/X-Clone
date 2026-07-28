package logic_core.app.usecase.tweet;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.RetweetRequest;
import logic_core.app.dto.response.TweetResponse;
import logic_core.app.dto.response.UserSummaryResponse;
import logic_core.app.mapper.TweetMapper;
import logic_core.app.mapper.UserSummaryResponseMapper;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.CurrentAuthContext;
import logic_core.app.security.SessionUserContext;
import logic_core.common.exception.AppException;
import logic_core.common.exception.DatabaseException;
import logic_core.common.exception.NotFoundException;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.tweetEvent.TweetRetweetedEvent;
import logic_core.domain.model.TweetModel;
import logic_core.domain.policy.InteractionPolicy;
import logic_core.domain.repository.TweetRepository;
import logic_core.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class RetweetUseCase
{
    @NonNull private final InteractionPolicy interactionPolicy;
    @NonNull private final TweetRepository tweetRepository;
    @NonNull private final UserRepository userRepository;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<TweetResponse> execute(RetweetRequest request)
    {
        if (request == null || request.tweetId() == null) return Result.failure("Tweet ID is required.");

        OffsetDateTime now = timeProvider.now();

        try
        {
            SessionUserContext context =
                    lockOrchestrator.lockAndGetContextByToken(
                            request.sessionToken()
                    );

            UUID currentUserId = context.lockedUser().getId();

            TweetModel originalTweet = tweetRepository.findActiveByIdForUpdate(request.tweetId())
                    .orElseThrow(() -> new NotFoundException("Tweet not found or deleted."));

            interactionPolicy.validateRetweet(currentUserId, originalTweet.getAuthorId(), originalTweet.getId());

            TweetModel retweet = TweetModel.builder()
                    .id(UUID.randomUUID())
                    .authorId(currentUserId)
                    .retweetedTweetId(originalTweet.getId())
                    .publishedAt(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            TweetModel savedRetweet = tweetRepository.save(retweet)
                    .orElseThrow(() -> new DatabaseException("Failed to save retweet."));

            TweetModel updatedOriginal = originalTweet.toBuilder()
                    .retweetCount(originalTweet.getRetweetCount() + 1)
                    .updatedAt(now)
                    .build();
            tweetRepository.update(updatedOriginal);

            eventPublisher.publish(new TweetRetweetedEvent(
                    originalTweet.getId(),
                    savedRetweet.getId(),
                    currentUserId,
                    now
            ));

            return Result.success(toResponse(savedRetweet));
        }
        catch (AppException e)
        {
            return Result.failure(e.getMessage());
        }
        catch (Exception e)
        {

            return Result.failure("Failed to process retweet.");
        }
    }

    private TweetResponse toResponse(TweetModel tweet)
    {
        UserSummaryResponse authorSummary = userRepository.findById(tweet.getAuthorId())
                .map(UserSummaryResponseMapper::toResponse)
                .orElse(null);

        TweetResponse retweetedTweet = tweet.getRetweetedTweetId() != null ?
                tweetRepository.findById(tweet.getRetweetedTweetId()).map(this::toShallowResponse).orElse(null) : null;

        return TweetMapper.toResponse(tweet, authorSummary, null, retweetedTweet, null);
    }

    private TweetResponse toShallowResponse(TweetModel tweet)
    {
        return TweetMapper.toResponse(tweet, null, null, null, null);
    }
}
