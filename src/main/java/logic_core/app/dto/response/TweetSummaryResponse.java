package logic_core.app.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TweetSummaryResponse(
        UUID id,
        UserSummaryResponse author,
        String content,
        OffsetDateTime createdAt,
        OffsetDateTime publishedAt,
        boolean deleted,
        boolean pinned,
        boolean edited,
        TweetStatsResponse stats,
        TweetViewerStateResponse viewerState
) {
}
