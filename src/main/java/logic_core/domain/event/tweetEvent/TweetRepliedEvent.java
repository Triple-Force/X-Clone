package logic_core.domain.event.tweetEvent;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class TweetRepliedEvent extends DomainEvent implements TweetEvent
{
    UUID replyTweetId;
    UUID parentTweetId;
    UUID replierId;
    UUID parentAuthorId;
    String content;

    public TweetRepliedEvent(UUID replyTweetId,
                             UUID parentTweetId,
                             UUID replierId,
                             UUID parentAuthorId,
                             String content,
                             OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.replyTweetId = Objects.requireNonNull(replyTweetId);
        this.parentTweetId = Objects.requireNonNull(parentTweetId);
        this.replierId = Objects.requireNonNull(replierId);
        this.parentAuthorId = Objects.requireNonNull(parentAuthorId);
        this.content = Objects.requireNonNull(content);
    }
}
