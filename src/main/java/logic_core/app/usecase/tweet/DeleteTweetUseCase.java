package logic_core.app.usecase.tweet;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.DeleteTweetRequest;
import logic_core.app.dto.response.TweetResponse;
import logic_core.app.dto.response.UserSummaryResponse;
import logic_core.app.mapper.TweetMapper;
import logic_core.app.mapper.UserSummaryResponseMapper;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.CurrentAuthContext;
import logic_core.app.security.SessionUserContext;
import logic_core.common.exception.ConflictException;
import logic_core.common.exception.ForbiddenException;
import logic_core.common.exception.NotFoundException;
import logic_core.common.exception.ValidationException;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.tweetEvent.TweetDeletedEvent;
import logic_core.domain.model.TweetModel;
import logic_core.domain.model.UserModel;
import logic_core.domain.repository.TweetRepository;
import logic_core.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class DeleteTweetUseCase
{
    @NonNull
    private final TweetRepository tweetRepository;
    @NonNull private final UserRepository userRepository;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<TweetResponse> execute(DeleteTweetRequest request)
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

            TweetModel tweet =
                    tweetRepository.findActiveById(request.tweetId())
                            .orElseThrow(() ->
                                    new RuntimeException("Tweet not found or already deleted.")
                            );

            if (!tweet.getAuthorId().equals(currentUserId))
            {
                return Result.failure("You are not authorized to delete this tweet.");
            }

            tweetRepository.softDelete(tweet.getId());

            TweetModel deletedTweet =
                    tweetRepository.findById(tweet.getId())
                            .orElseThrow(() ->
                                    new RuntimeException("Failed to reload deleted tweet.")
                            );

            TweetResponse response =
                    buildDeletedResponse(deletedTweet);

            eventPublisher.publish(
                    new TweetDeletedEvent(
                            deletedTweet.getId(),
                            deletedTweet.getAuthorId(),
                            timeProvider.now()
                    )
            );

            return Result.success(response);
        }
        catch (ValidationException | ForbiddenException | ConflictException | NotFoundException e)
        {
            return Result.failure(e.getMessage());
        }
    }

    private TweetResponse buildDeletedResponse(TweetModel tweet)
    {
        UserModel author =
                userRepository.findById(tweet.getAuthorId())
                        .orElse(null);

        UserSummaryResponse authorSummary =
                author != null
                        ? UserSummaryResponseMapper.toResponse(author)
                        : null;

        return TweetMapper.toResponse(
                tweet,
                authorSummary,
                null,
                null,
                null,
                null
        );
    }
}