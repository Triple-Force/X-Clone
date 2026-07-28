package logic_core.app.dto.response;

import java.util.List;

public record TweetThreadResponse(
        TweetSummaryResponse rootTweet,
        List<TweetSummaryResponse> replies
) {}
