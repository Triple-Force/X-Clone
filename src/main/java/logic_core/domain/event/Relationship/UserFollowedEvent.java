package logic_core.domain.event.Relationship;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class UserFollowedEvent extends DomainEvent implements RelationshipEvent
{
    UUID actorId;
    UUID targetId;

    public UserFollowedEvent(UUID actorId,
                             UUID targetId,
                             OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.actorId = Objects.requireNonNull(actorId);
        this.targetId = Objects.requireNonNull(targetId);
    }
}
