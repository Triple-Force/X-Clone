package logic_core.app.dto.response;

import java.util.List;

public record HomeTimelineResponse(
        List<TweetDetailResponse> tweets,
        int limit,
        int offset,
        boolean hasMore
) {}