package logic_core.app.systemMessage;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface SystemMessageService
{
    void sendWelcomeMessage(
            UUID userId,
            String username,
            String email,
            OffsetDateTime occurredAt
    );

    void sendLoginSuccessMessage(
            UUID userId,
            String username,
            UUID sessionId,
            OffsetDateTime occurredAt
    );

    void sendLogoutSuccessMessage(
            UUID userId,
            String username,
            UUID sessionId,
            OffsetDateTime occurredAt
    );

    void send(SystemMessageModel message);
}
