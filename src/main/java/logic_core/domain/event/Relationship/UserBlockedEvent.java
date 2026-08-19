package logic_core.domain.event.Relationship;


import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class UserBlockedEvent extends DomainEvent implements RelationshipEvent
{
    UUID blockerId;
    UUID blockedId;

    public UserBlockedEvent(UUID blockerId,
                            UUID blockedId,
                            OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.blockerId = Objects.requireNonNull(blockerId);
        this.blockedId = Objects.requireNonNull(blockedId);
    }
}