package logic_core.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public abstract class DomainEvent
{
    private final OffsetDateTime occurredAt;
    private final UUID eventId;

    protected DomainEvent(OffsetDateTime occurredAt)
    {
        this.occurredAt = Objects.requireNonNull(occurredAt);
        this.eventId = UUID.randomUUID();
    }


    public OffsetDateTime occurredAt()
    {
        return occurredAt;
    }
}
