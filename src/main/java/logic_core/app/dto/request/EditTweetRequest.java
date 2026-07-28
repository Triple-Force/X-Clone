package logic_core.app.dto.request;

import java.util.UUID;

public record EditTweetRequest(
        UUID tweetId,
        String content,
        String sessionToken
) {}