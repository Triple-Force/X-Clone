package logic_core.app.usecase.tweet;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.CreateTweetRequest;
import logic_core.app.dto.response.TweetResponse;
import logic_core.app.dto.response.UserSummaryResponse;
import logic_core.app.dto.validator.TweetValidator;
import logic_core.app.mapper.TweetMapper;
import logic_core.app.mapper.UserSummaryResponseMapper;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.SessionUserContext;
import logic_core.common.exception.ConflictException;
import logic_core.common.exception.ForbiddenException;
import logic_core.common.exception.NotFoundException;
import logic_core.common.exception.ValidationException;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.tweetEvent.TweetCreatedEvent;
import logic_core.domain.model.TweetModel;
import logic_core.domain.model.UserModel;
import logic_core.domain.policy.InteractionPolicy;
import logic_core.domain.repository.TweetRepository;
import logic_core.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class CreateTweetUseCase
{
    @NonNull private final TweetValidator validator;
    @NonNull private final InteractionPolicy interactionPolicy;
    @NonNull private final TweetRepository tweetRepository;
    @NonNull private final UserRepository userRepository;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<TweetResponse> execute(@NonNull CreateTweetRequest request)
    {
        try
        {
            SessionUserContext context =
                    lockOrchestrator.lockAndGetContextByToken(
                            request.sessionToken()
                    );

            UserModel currentUser = context.lockedUser();
            UUID currentUserId = currentUser.getId();

            validator.validateCreateTweet(
                    request.content(),
                    request.replyToId(),
                    request.quoteOfId(),
                    request.mediaIds(),
                    false,
                    request.scheduledAt()
            );

            interactionPolicy.validateCreate(
                    request.content(),
                    request.replyToId(),
                    request.quoteOfId(),
                    request.mediaIds(),
                    request.scheduledAt(),
                    currentUserId
            );

            TweetModel tweet = createTweetEntity(
                    request,
                    currentUserId
            );

            TweetModel savedTweet =
                    tweetRepository.save(tweet)
                            .orElseThrow(() ->
                                    new RuntimeException("Failed to save tweet.")
                            );

            TweetResponse response =
                    buildTweetResponse(savedTweet);

            eventPublisher.publish(
                    new TweetCreatedEvent(
                            savedTweet.getId(),
                            savedTweet.getAuthorId(),
                            savedTweet.getContent(),
                            savedTweet.getRepliedToTweetId(),
                            savedTweet.getQuotedTweetId(),
                            savedTweet.getMediaIds(),
                            timeProvider.now()
                    )
            );


            try
            {

            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw e;
            }

            return Result.success(response);
        }
        catch (ValidationException | ForbiddenException | ConflictException | NotFoundException e)
        {
            return Result.failure(e.getMessage());
        }
    }

    private TweetModel createTweetEntity(
            CreateTweetRequest request,
            UUID authorId)
    {
        return TweetModel.builder()
                .id(UUID.randomUUID())
                .authorId(authorId)
                .content(request.content())
                .repliedToTweetId(request.replyToId())
                .quotedTweetId(request.quoteOfId())
                .scheduledAt(request.scheduledAt())
                .publishedAt(timeProvider.now())
                .createdAt(timeProvider.now())
                .mediaIds(request.mediaIds())
                .build();
    }

    private TweetResponse buildTweetResponse(TweetModel tweet)
    {
        UserModel author =
                userRepository.findById(tweet.getAuthorId())
                        .orElseThrow(() ->
                                new RuntimeException("Author not found.")
                        );

        UserSummaryResponse authorSummary =
                UserSummaryResponseMapper.toResponse(author);

        TweetResponse repliedTweetResponse = null;

        if (tweet.getRepliedToTweetId() != null)
        {
            repliedTweetResponse =
                    tweetRepository.findById(tweet.getRepliedToTweetId())
                            .map(parent ->
                                    TweetMapper.toResponse(
                                            parent,
                                            null,
                                            null,
                                            null,
                                            null
                                    )
                            )
                            .orElse(null);
        }

        TweetResponse quotedTweetResponse = null;

        if (tweet.getQuotedTweetId() != null)
        {
            quotedTweetResponse =
                    tweetRepository.findById(tweet.getQuotedTweetId())
                            .map(parent ->
                                    TweetMapper.toResponse(
                                            parent,
                                            null,
                                            null,
                                            null,
                                            null
                                    )
                            )
                            .orElse(null);
        }

        return TweetMapper.toResponse(
                tweet,
                authorSummary,
                repliedTweetResponse,
                null,
                quotedTweetResponse
        );
    }
}