package logic_core.app.dto.request;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CreateTweetRequest(
        String content,
        UUID replyToId,
        UUID quoteOfId,
         List<UUID> mediaIds,
        OffsetDateTime scheduledAt,
        String sessionToken
) {}
