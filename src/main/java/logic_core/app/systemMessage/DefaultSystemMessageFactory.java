package logic_core.app.systemMessage;

import logic_core.common.util.IdUtil;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DefaultSystemMessageFactory implements SystemMessageFactory
{
    @Override
    public SystemMessageModel createWelcomeMessage(
            UUID userId,
            String username,
            String email,
            OffsetDateTime occurredAt
    )
    {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");

        return new SystemMessageModel(
                IdUtil.newUUID(),
                userId,
                SystemMessageType.WELCOME,
                "Welcome to X-Clone",
                "Hi " + username + ", your account has been created successfully.",
                SystemMessagePriority.NORMAL,
                occurredAt,
                Map.of(
                        "username", username,
                        "email", email
                )
        );
    }

    @Override
    public SystemMessageModel createLoginSuccessMessage(
            UUID userId,
            String username,
            UUID sessionId,
            OffsetDateTime occurredAt
    )
    {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");

        return new SystemMessageModel(
                IdUtil.newUUID(),
                userId,
                SystemMessageType.LOGIN_SUCCESS,
                "Login successful",
                "Hi " + username + ", your login was successful.",
                SystemMessagePriority.LOW,
                occurredAt,
                Map.of(
                        "username", username,
                        "sessionId", sessionId.toString()
                )
        );
    }

    @Override
    public SystemMessageModel createLogoutSuccessMessage(
            UUID userId,
            String username,
            UUID sessionId,
            OffsetDateTime occurredAt
    )
    {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");

        return new SystemMessageModel(
                IdUtil.newUUID(),
                userId,
                SystemMessageType.LOGOUT_SUCCESS,
                "Logout successful",
                "Goodbye " + username + ", your session has been closed successfully.",
                SystemMessagePriority.LOW,
                occurredAt,
                Map.of(
                        "username", username,
                        "sessionId", sessionId.toString()
                )
        );
    }
}
