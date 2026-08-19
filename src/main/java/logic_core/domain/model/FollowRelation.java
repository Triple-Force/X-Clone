package logic_core.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowRelation
{
    private UUID followerId;
    private UUID followingId;
    private OffsetDateTime createdAt;

    private FollowRelation(UUID followerId, UUID followingId, OffsetDateTime createdAt)
    {
        this.followerId = Objects.requireNonNull(followerId, "Follow.followerId cannot be null");
        this.followingId = Objects.requireNonNull(followingId, "Follow.followingId cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Follow.createdAt cannot be null");

        if (followerId.equals(followingId))
        {
            throw new IllegalArgumentException("Cannot follow yourself.");
        }
    }

    public static FollowRelation create(UUID followerId, UUID followingId, OffsetDateTime createdAt)
    {
        return new FollowRelation(followerId, followingId, createdAt);
    }
}
