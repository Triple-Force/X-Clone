package logic_core.app.dto.request;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CreateTweetRequest(
        String content,
        UUID replyToId,
        UUID quoteOfId,
        OffsetDateTime scheduledAt,
        String sessionToken,
        List<String> mediaUrls
) {}