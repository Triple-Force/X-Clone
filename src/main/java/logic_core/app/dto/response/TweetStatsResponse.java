package logic_core.app.dto.response;

public record TweetStatsResponse(
        long likeCount,
        long replyCount,
        long retweetCount
) {}
