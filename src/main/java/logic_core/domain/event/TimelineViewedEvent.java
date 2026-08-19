package logic_core.domain.event;

import logic_core.domain.repository.TimelineType;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class TimelineViewedEvent extends DomainEvent
{
    private final UUID actorId;

    private final UUID targetUserId;

    private final TimelineType timelineType;

    private final int resultCount;

    public TimelineViewedEvent(UUID actorId,
                               UUID targetUserId,
                               TimelineType timelineType,
                               int resultCount,
                               OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.actorId = actorId;
        this.targetUserId = targetUserId;
        this.timelineType = timelineType;
        this.resultCount = resultCount;
    }
}