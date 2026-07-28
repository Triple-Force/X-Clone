package logic_core.app.facade;

import logic_core.app.dto.request.*;
import logic_core.app.dto.response.LikeResponse;
import logic_core.app.dto.response.TweetResponse;
import logic_core.app.usecase.tweet.*;
import logic_core.common.result.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TweetFacade
{
    @NonNull private final CreateTweetUseCase createTweetUseCase;
    @NonNull private final DeleteTweetUseCase deleteTweetUseCase;
    @NonNull private final EditTweetUseCase editTweetUseCase;
    @NonNull private final LikeTweetUseCase likeTweetUseCase;
    @NonNull private final ReplyTweetUseCase replyTweetUseCase;
    @NonNull private final RetweetUseCase retweetUseCase;
    @NonNull private final UnlikeTweetUseCase unlikeTweetUseCase;

    public Result<TweetResponse> createTweet(CreateTweetRequest request)
    {
        return createTweetUseCase.execute(request);
    }

    public Result<TweetResponse> deleteTweet(DeleteTweetRequest request)
    {
        return deleteTweetUseCase.execute(request);
    }

    public Result<TweetResponse> editTweet(EditTweetRequest request)
    {
        return editTweetUseCase.execute(request);
    }

    public Result<LikeResponse> likeTweet(LikeTweetRequest request)
    {
        return likeTweetUseCase.execute(request);
    }

    public Result<TweetResponse> replyTweet(ReplyTweetRequest request)
    {
        return replyTweetUseCase.execute(request);
    }

    public Result<TweetResponse> retweet(RetweetRequest request)
    {
        return retweetUseCase.execute(request);
    }

    public Result<LikeResponse> unlikeTweet(UnlikeTweetRequest request)
    {
        return unlikeTweetUseCase.execute(request);
    }
}
