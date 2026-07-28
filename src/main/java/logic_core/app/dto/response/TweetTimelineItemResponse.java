package logic_core.app.dto.response;

import enums.TimelineItemType;

public record TweetTimelineItemResponse(
        TimelineItemType type,
        TweetSummaryResponse tweet,
        UserSummaryResponse actor
) {}
