package logic_core.domain.event.tweetEvent;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class TweetRetweetedEvent extends DomainEvent implements TweetEvent
{
    UUID originalTweetId;
    UUID retweetedTweetId;
    UUID userId;

    public TweetRetweetedEvent(UUID originalTweetId,
                               UUID retweetedTweetId,
                               UUID userId,
                               OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.originalTweetId = Objects.requireNonNull(originalTweetId);
        this.retweetedTweetId = Objects.requireNonNull(retweetedTweetId);
        this.userId = Objects.requireNonNull(userId);
    }
}


