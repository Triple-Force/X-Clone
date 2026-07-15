package logic_core.app.dto.response;

import java.util.List;

public record FeedResponse(
        List<TweetResponse> tweets,
        int page,
        int size,
        boolean hasNext
) {}
