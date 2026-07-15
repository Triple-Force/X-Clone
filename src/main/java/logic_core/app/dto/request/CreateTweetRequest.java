package logic_core.app.dto.request;

import java.util.UUID;

public record CreateTweetRequest(
        String content,
        UUID replyToId,
        UUID quoteOfId
        // List<UUID> mediaIds
) {}
