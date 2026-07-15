package logic_core.domain.event.authentication;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class UserLoggedOutEvent extends DomainEvent implements AuthenticationEvent
{
    private final UUID userId;
    private final String username;
    private final UUID sessionId;

    public UserLoggedOutEvent(UUID userId,
                              String username,
                              UUID sessionId,
                              OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.userId = userId;
        this.username = username;
        this.sessionId = sessionId;
    }
}
