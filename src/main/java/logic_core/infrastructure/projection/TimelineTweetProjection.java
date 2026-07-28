package logic_core.infrastructure.projection;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TimelineTweetProjection(

        UUID tweetId,
        UUID authorId,
        String username,
        String displayName,
        String avatarUrl,
        String content,
        long likeCount,
        long replyCount,
        long retweetCount,
        OffsetDateTime publishedAt

) {}