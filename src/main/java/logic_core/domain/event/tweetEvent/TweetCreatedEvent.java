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
    private final UUID tweetId;
    private final UUID authorId;
    private final String content;
    private final UUID repliedToTweetId;
    private final UUID quotedTweetId;
    private final List<UUID> mediaIds;

    public TweetCreatedEvent(UUID tweetId,
                             UUID authorId,
                             String content,
                             UUID repliedToTweetId,
                             UUID quotedTweetId,
                             List<UUID> mediaIds,
                             OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.tweetId = Objects.requireNonNull(tweetId);
        this.authorId = Objects.requireNonNull(authorId);
        this.content = Objects.requireNonNull(content);
        this.repliedToTweetId = repliedToTweetId;
        this.quotedTweetId = quotedTweetId;
        this.mediaIds = mediaIds == null ? List.of() : List.copyOf(mediaIds);
    }
}
