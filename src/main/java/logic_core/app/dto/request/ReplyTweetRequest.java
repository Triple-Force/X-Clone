package logic_core.app.dto.request;

import java.util.List;
import java.util.UUID;

public record ReplyTweetRequest(
        UUID parentTweetId,
        String text,
        List<String> mediaUrls,
        String sessionToken
) {}