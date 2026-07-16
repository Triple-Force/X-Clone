package logic_core.domain.event.Relationship;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class UserUnblockedEvent extends DomainEvent implements RelationshipEvent
{
    UUID actorId;
    UUID targetId;

    public UserUnblockedEvent(UUID actorId,
                              UUID targetId,
                              OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.actorId = Objects.requireNonNull(actorId, "actorId must not be null.");
        this.targetId = Objects.requireNonNull(targetId, "targetId must not be nul.");
    }
}
