package logic_core.domain.event.Relationship;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class UserUnmutedEvent extends DomainEvent implements RelationshipEvent
{
    UUID actorId;
    UUID tagetId;

    public UserUnmutedEvent(UUID actorId,
                            UUID tagetId,
                            OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.actorId = Objects.requireNonNull(actorId);
        this.tagetId = Objects.requireNonNull(tagetId);
    }
}
