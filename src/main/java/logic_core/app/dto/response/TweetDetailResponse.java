package logic_core.app.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TweetDetailResponse(
        UUID id,
        UserSummaryResponse author,
        String content,
        boolean deleted,
        boolean pinned,
        boolean edited,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime publishedAt,
        UUID repliedToTweetId,
        UUID retweetedTweetId,
        UUID quotedTweetId,
        TweetStatsResponse stats,
        TweetViewerStateResponse viewerState,
        List<UserSummaryResponse> mentions,
        List<MediaResponse> media,
        PollResponse poll,
        List<HashtagResponse> hashtags,
        TweetSummaryResponse repliedToTweet,
        TweetSummaryResponse retweetedTweet,
        TweetSummaryResponse quotedTweet,
        List<TweetEditResponse> editHistory
) {
}
