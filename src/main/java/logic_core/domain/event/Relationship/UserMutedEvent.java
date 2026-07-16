package logic_core.domain.event.Relationship;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class UserMutedEvent extends DomainEvent implements RelationshipEvent
{
    UUID muterId;
    UUID mutedId;

    public UserMutedEvent(UUID muterId,
                          UUID mutedId,
                          OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.muterId = Objects.requireNonNull(muterId);
        this.mutedId = Objects.requireNonNull(mutedId);
    }
}
