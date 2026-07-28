package logic_core.app.dto.timeline;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record TimelineTweet(
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