package logic_core.app.systemMessage;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface SystemMessageFactory
{
    SystemMessageModel createWelcomeMessage(
            UUID userId,
            String username,
            String email,
            OffsetDateTime occurredAt
    );

    SystemMessageModel createLoginSuccessMessage(
            UUID userId,
            String username,
            UUID sessionId,
            OffsetDateTime occurredAt
    );

    SystemMessageModel createLogoutSuccessMessage(
            UUID userId,
            String username,
            UUID sessionId,
            OffsetDateTime occurredAt
    );
}
