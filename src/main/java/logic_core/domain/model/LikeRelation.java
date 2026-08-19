package logic_core.domain.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LikeRelation
{
    private UUID userId;
    private UUID tweetId;
    private OffsetDateTime createdAt;

    public static LikeRelation create(UUID tweetId, UUID userId)
    {

        Objects.requireNonNull(tweetId, "LikeRelation.tweeterId cannot be null");
        Objects.requireNonNull(userId, "LikeRelation.userId cannot be null");

        return builder()
                .userId(userId)
                .tweetId(tweetId)
                .createdAt(OffsetDateTime.now())
                .build();
    }
}
