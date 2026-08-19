package logic_core.app.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TweetInfoResponse(

        UUID tweetId,
        UUID authorId,
        String username,
        String displayName,
        String avatarUrl,
        String content,
        List<String> mediaUrls,
        int likeCount,
        int replyCount,
        int retweetCount,
        boolean liked,
        boolean bookmarked,
        OffsetDateTime createdAt
) {}