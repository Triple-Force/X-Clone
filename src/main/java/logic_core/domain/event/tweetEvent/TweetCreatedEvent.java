package logic_core.domain.event.tweetEvent;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
public class TweetCreatedEvent extends DomainEvent implements TweetEvent
{
    UUID authorId;
    String content;
    UUID parentTweetId;
    List<String> mediaUrls;

    public TweetCreatedEvent(UUID authorId,
                             String content,
                             UUID parentTweetId,
                             List<String> mediaUrls,
                             OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.authorId = Objects.requireNonNull(authorId);
        this.content = Objects.requireNonNull(content);
        this.parentTweetId = Objects.requireNonNull(parentTweetId);
        this.mediaUrls = Objects.requireNonNull(mediaUrls);
    }
}
