package logic_core.app.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TweetEditResponse(
        UUID id,
        UUID tweetId,
        String previousContent,
        String newContent,
        OffsetDateTime editedAt
) {}
