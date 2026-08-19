package logic_core.domain.event.pollEvent;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
public class PollCreatedEvent extends DomainEvent implements PollEvent
{

    UUID pollId;
    UUID authorId;
    String question;
    List<String> options;
    OffsetDateTime expirationDate;

    public PollCreatedEvent(UUID pollId,
                            UUID authorId,
                            String question,
                            List<String> options,
                            OffsetDateTime expirationDate,
                            OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.pollId = Objects.requireNonNull(pollId);
        this.authorId = Objects.requireNonNull(authorId);
        this.question = Objects.requireNonNull(question);
        this.options = Objects.requireNonNull(options);
        this.expirationDate = Objects.requireNonNull(expirationDate);
    }
}