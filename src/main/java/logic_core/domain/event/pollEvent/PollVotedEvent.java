package logic_core.domain.event.pollEvent;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class PollVotedEvent extends DomainEvent implements PollEvent
{
    UUID pollId;
    UUID optionId;
    UUID voterId;

    public PollVotedEvent(UUID pollId,
                          UUID optionId,
                          UUID voterId,
                          OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.pollId = Objects.requireNonNull(pollId);
        this.optionId = Objects.requireNonNull(optionId);
        this.voterId = Objects.requireNonNull(voterId);
    }
}
