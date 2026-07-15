package logic_core.app.dto.response;

import java.util.List;

public record SearchResultResponse(
        List<UserResponse> users,
        List<TweetResponse> tweets,
        List<HashtagResponse> hashtags
) {}
