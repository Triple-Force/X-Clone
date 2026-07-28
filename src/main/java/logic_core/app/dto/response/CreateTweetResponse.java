package logic_core.app.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CreateTweetResponse(
        UUID tweetId,
        UUID authorId,
        String content,
        UUID replyToId,
        UUID quoteOfId,
        List<UUID> mediaIds,
        OffsetDateTime createdAt,
        OffsetDateTime scheduledAt
) {}
