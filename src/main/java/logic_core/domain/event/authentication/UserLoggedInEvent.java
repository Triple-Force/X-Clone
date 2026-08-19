package logic_core.domain.event.authentication;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class UserLoggedInEvent extends DomainEvent implements AuthenticationEvent
{
    private final UUID userId;
    private final String username;
    private final UUID sessionId;

    public UserLoggedInEvent(UUID userId,
                             String username,
                             UUID sessionId,
                             OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.userId = Objects.requireNonNull(userId);
        this.username = Objects.requireNonNull(username);
        this.sessionId = Objects.requireNonNull(sessionId);
    }
}
