package logic_core.app.dto.request;

import java.util.UUID;

public record UnlikeTweetRequest(
        UUID tweetId,
        String sessionToken
) {}