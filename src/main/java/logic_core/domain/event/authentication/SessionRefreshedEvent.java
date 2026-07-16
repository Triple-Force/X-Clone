package logic_core.domain.event.authentication;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class SessionRefreshedEvent extends DomainEvent implements AuthenticationEvent
{
    private final UUID userId;
    private final UUID oldSessionId;
    private final UUID newSessionId;

    public SessionRefreshedEvent(UUID userId,
                                 UUID oldSessionId,
                                 UUID newSessionId,
                                 OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.userId = userId;
        this.oldSessionId = oldSessionId;
        this.newSessionId = newSessionId;
    }
}
