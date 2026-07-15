package logic_core.domain.event.userEvent;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class UserRegisteredEvent extends DomainEvent implements AccountEvent
{
    private final UUID userId;
    private final String username;
    private final String email;

    public UserRegisteredEvent(UUID userId,
                               String username,
                               String email,
                               OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.username = Objects.requireNonNull(username, "username must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
    }
}
