package logic_core.app.usecase.tweet;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.ReplyTweetRequest;
import logic_core.app.dto.response.TweetResponse;
import logic_core.app.dto.response.UserSummaryResponse;
import logic_core.app.dto.validator.TweetValidator;
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
import logic_core.domain.event.tweetEvent.TweetRepliedEvent;
import logic_core.domain.model.TweetModel;
import logic_core.domain.policy.InteractionPolicy;
import logic_core.domain.repository.TweetRepository;
import logic_core.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class ReplyTweetUseCase
{
    @NonNull private final InteractionPolicy interactionPolicy;
    @NonNull private final TweetRepository tweetRepository;
    @NonNull private final UserRepository userRepository;
    @NonNull private final TweetValidator tweetValidator;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<TweetResponse> execute(ReplyTweetRequest request)
    {
        if (request == null) return Result.failure("Request cannot be null.");

        OffsetDateTime now = timeProvider.now();

        try
        {
            SessionUserContext context =
                    lockOrchestrator.lockAndGetContextByToken(
                            request.sessionToken()
                    );

            UUID currentUserId = context.lockedUser().getId();

            tweetValidator.validateReplyTweet(request.parentTweetId(), request.text(), request.mediaIds());

            TweetModel parentTweet = tweetRepository.findActiveByIdForUpdate(request.parentTweetId())
                    .orElseThrow(() -> new NotFoundException("Parent tweet not found or deleted."));

            interactionPolicy.validateReply(currentUserId, parentTweet.getAuthorId());

            TweetModel reply = TweetModel.builder()
                    .id(UUID.randomUUID())
                    .authorId(currentUserId)
                    .content(request.text())
                    .repliedToTweetId(parentTweet.getId())
                    .mediaIds(request.mediaIds())
                    .publishedAt(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            TweetModel savedReply = tweetRepository.save(reply)
                    .orElseThrow(() -> new DatabaseException("Failed to save reply."));

            TweetModel updatedParent = parentTweet.toBuilder()
                    .replyCount(parentTweet.getReplyCount() + 1)
                    .updatedAt(now)
                    .build();
            tweetRepository.update(updatedParent);

            eventPublisher.publish(new TweetRepliedEvent(
                    savedReply.getId(),
                    parentTweet.getId(),
                    currentUserId,
                    parentTweet.getAuthorId(),
                    request.text(),
                    timeProvider.now()
            ));

            return Result.success(toResponse(savedReply));
        }
        catch (AppException e)
        {
            return Result.failure(e.getMessage());
        }
        catch (Exception e)
        {

            return Result.failure("An unexpected error occurred while replying.");
        }
    }

    private TweetResponse toResponse(TweetModel tweet)
    {
        UserSummaryResponse authorSummary = userRepository.findById(tweet.getAuthorId())
                .map(UserSummaryResponseMapper::toResponse)
                .orElse(null);

        TweetResponse repliedTo = tweet.getRepliedToTweetId() != null ?
                tweetRepository.findActiveById(tweet.getRepliedToTweetId()).map(this::toShallowResponse).orElse(null) : null;

        return TweetMapper.toResponse(tweet, authorSummary, repliedTo, null, null);
    }

    private TweetResponse toShallowResponse(TweetModel tweet)
    {
        return TweetMapper.toResponse(tweet, null, null, null, null);
    }
}
