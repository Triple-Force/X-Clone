package logic_core.domain.event;

import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Getter
public class HashtagExtractedEvent extends DomainEvent
{
    private final List<String> tags;

    public HashtagExtractedEvent(List<String> tags, OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.tags = Objects.requireNonNull(List.copyOf(tags));
    }
}