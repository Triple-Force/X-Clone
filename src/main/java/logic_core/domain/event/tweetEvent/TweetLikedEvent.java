package logic_core.domain.event.tweetEvent;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class TweetLikedEvent extends DomainEvent implements TweetEvent
{

    UUID tweetId;
    UUID userId;

    public TweetLikedEvent(UUID tweetId,
                           UUID userId,
                           OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.tweetId = Objects.requireNonNull(tweetId);
        this.userId = Objects.requireNonNull(userId);
    }
}
