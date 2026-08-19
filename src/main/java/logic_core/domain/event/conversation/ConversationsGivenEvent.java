package logic_core.domain.event.conversation;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class ConversationsGivenEvent extends DomainEvent implements ConversationEvent
{
    private final UUID userId;
    private final int page;
    private final int pageSize;
    private final int returnedCount;

    public ConversationsGivenEvent(UUID userId,
                                   int page,
                                   int pageSize,
                                   int returnedCount,
                                   OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.userId = userId;
        this.page = page;
        this.pageSize = pageSize;
        this.returnedCount = returnedCount;
    }

}
