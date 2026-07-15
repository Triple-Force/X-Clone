package logic_core.app.dto.view;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TweetCardView(
        UUID id,
        UserCardView author,
        String text,
        OffsetDateTime createdAt,
        int likesCount,
        int repliesCount,
        int retweetsCount,
        boolean liked,
        boolean retweeted,
        boolean pinned
) {}
