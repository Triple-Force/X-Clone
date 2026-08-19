package logic_core.app.mapper;

import logic_core.app.dto.response.HashtagResponse;
import logic_core.app.dto.response.SearchResultResponse;
import logic_core.app.dto.response.TweetResponse;
import logic_core.app.dto.response.UserResponse;

import java.util.List;

public final class SearchResultMapper
{
    private SearchResultMapper()
    {

    }

    public static SearchResultResponse toResponse(
            List<UserResponse> users,
            List<TweetResponse> tweets,
            List<HashtagResponse> hashtags)
    {
        return new SearchResultResponse(users, tweets, hashtags);
    }
}
