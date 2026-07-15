package logic_core.app.mapper;

import logic_core.app.dto.response.FeedResponse;
import logic_core.app.dto.response.TweetResponse;
import java.util.List;

public final class FeedMapper
{
    private FeedMapper()
    {

    }

    public static FeedResponse toResponse(
            List<TweetResponse> tweets,
            int page,
            int size,
            boolean hasNext)
    {
        return new FeedResponse(tweets, page, size, hasNext);
    }
}
