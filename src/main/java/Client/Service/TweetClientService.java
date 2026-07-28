package Client.Service;

import Client.ClientApplicationContext;
import Client.session.ClientSession;
import Client.transport.SocketClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import logic_core.app.dto.request.*;
import logic_core.app.dto.response.LikeResponse;
import logic_core.app.dto.response.TweetResponse;
import logic_core.common.result.Result;
import logic_core.infrastructure.transport.RequestEnvelope;
import logic_core.infrastructure.transport.RequestType;
import logic_core.infrastructure.transport.ResponseEnvelope;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
public final class TweetClientService
{
    private final SocketClient socketClient;
    private final ClientSession session;
    private final ExecutorService networkExecutor;
    private final Gson gson;

    public TweetClientService(ClientApplicationContext context)
    {
        this(
                context.socketClient(),
                context.session(),
                context.networkExecutor(),
                new GsonBuilder().serializeNulls().create()
        );
    }

    public CompletableFuture<Result<TweetResponse>> createTweet(
            String content,
            UUID replyToId,
            UUID quoteOfId,
            List<UUID> mediaIds,
            OffsetDateTime scheduledAt)
    {
        CreateTweetRequest request = new CreateTweetRequest(content, replyToId, quoteOfId, mediaIds, scheduledAt, session.getToken());

        return execute(
                RequestType.TWEET_CREATE,
                request,
                TweetResponse.class
        );
    }

    public CompletableFuture<Result<TweetResponse>> editTweet(
            UUID tweetId,
            String content)
    {
        EditTweetRequest request = new EditTweetRequest(tweetId, content, session.getToken());

        return execute(
                RequestType.TWEET_EDIT,
                request,
                TweetResponse.class
        );
    }

    public CompletableFuture<Result<TweetResponse>> deleteTweet(
            UUID tweetId)
    {
        DeleteTweetRequest request = new DeleteTweetRequest(tweetId, session.getToken());

        return execute(
                RequestType.TWEET_DELETE,
                request,
                TweetResponse.class
        );
    }

    public CompletableFuture<Result<TweetResponse>> replyTweet(
            UUID parentTweetId,
            String content,
            List<UUID> mediaIds)
    {
        ReplyTweetRequest request = new ReplyTweetRequest(parentTweetId, content, mediaIds, session.getToken());

        return execute(
                RequestType.TWEET_REPLY,
                request,
                TweetResponse.class
        );
    }

    public CompletableFuture<Result<TweetResponse>> retweet(
            UUID tweetId,
            String content)
    {
        RetweetRequest request = new RetweetRequest(tweetId, session.getToken());

        return execute(
                RequestType.TWEET_RETWEET,
                request,
                TweetResponse.class
        );
    }

    public CompletableFuture<Result<LikeResponse>> likeTweet(
            UUID tweetId)
    {
        LikeTweetRequest request = new LikeTweetRequest(tweetId, session.getToken());

        return execute(
                RequestType.TWEET_LIKE,
                request,
                LikeResponse.class
        );
    }

    public CompletableFuture<Result<LikeResponse>> unlikeTweet(
            UUID tweetId)
    {
        UnlikeTweetRequest request = new UnlikeTweetRequest(tweetId, session.getToken());

        return execute(
                RequestType.TWEET_UNLIKE,
                request,
                LikeResponse.class
        );
    }

    private <T> CompletableFuture<Result<T>> execute(
            RequestType type,
            Object request,
            Class<T> responseClass)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try
            {
                socketClient.connect();

                JsonElement payload = gson.toJsonTree(request);

                RequestEnvelope envelope =
                        new RequestEnvelope(
                                UUID.randomUUID(),
                                type,
                                payload,
                                session.getToken()
                        );

                ResponseEnvelope response =
                        socketClient.send(envelope);

                if (response == null)
                {
                    return Result.failure("EMPTY_RESPONSE");
                }

                if (!response.isSuccess())
                {
                    return Result.failure(response.errorMessage());
                }

                T data =
                        gson.fromJson(
                                response.getData(),
                                responseClass
                        );

                return Result.success(data);
            }
            catch (Exception e)
            {
                return Result.failure(e.getMessage());
            }
        }, networkExecutor);
    }
}