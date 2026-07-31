package logic_core.app.dto.request;

import java.util.UUID;

public record GetRepliesRequest(
        UUID tweetId,
        String token
) {
}