package logic_core.domain.event.Relationship;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class UserUnfollowedEvent extends DomainEvent implements RelationshipEvent
{

    UUID followerId;
    UUID followingId;

    public UserUnfollowedEvent(UUID followerId,
                               UUID followingId,
                               OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.followerId = Objects.requireNonNull(followerId);
        this.followingId = Objects.requireNonNull(followingId);
    }
}
