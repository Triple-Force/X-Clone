package logic_core.domain.event.tweetEvent;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class TweetEditedEvent extends DomainEvent implements TweetEvent
{
    private final UUID tweetId;
    private final UUID editorId;
    private final String previousContent;
    private final String newContent;

    public TweetEditedEvent(UUID tweetId,
                            UUID editorId,
                            String previousContent,
                            String newContent,
                            OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.tweetId = Objects.requireNonNull(tweetId);
        this.editorId = Objects.requireNonNull(editorId);
        this.previousContent = Objects.requireNonNull(previousContent);
        this.newContent = Objects.requireNonNull(newContent);
    }
}