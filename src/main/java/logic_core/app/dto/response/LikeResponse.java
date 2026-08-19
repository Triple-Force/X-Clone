package logic_core.app.dto.response;

import java.util.UUID;

public record LikeResponse(
        UUID userId,
        UUID tweetId,
        boolean liked,
        long totalLikesCount
) {}
