package logic_core.domain.event.tweetEvent;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class TweetDeletedEvent extends DomainEvent
{
    UUID tweetId;
    UUID authorId;

    public TweetDeletedEvent(UUID tweetId,
                             UUID authorId,
                             OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.tweetId = Objects.requireNonNull(tweetId);
        this.authorId = Objects.requireNonNull(authorId);
    }
}
