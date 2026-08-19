package logic_core.app.systemMessage;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public class SystemMessageServiceImpl implements SystemMessageService
{
    private final SystemMessageFactory messageFactory;
    private final SystemMessageDispatcher dispatcher;

    public SystemMessageServiceImpl(
            SystemMessageFactory messageFactory,
            SystemMessageDispatcher dispatcher
    )
    {
        this.messageFactory = Objects.requireNonNull(messageFactory, "messageFactory must not be null");
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher must not be null");
    }

    @Override
    public void sendWelcomeMessage(
            UUID userId,
            String username,
            String email,
            OffsetDateTime occurredAt
    )
    {
        SystemMessageModel message = messageFactory.createWelcomeMessage(
                userId,
                username,
                email,
                occurredAt
        );
        dispatcher.dispatch(message);
    }

    @Override
    public void sendLoginSuccessMessage(
            UUID userId,
            String username,
            UUID sessionId,
            OffsetDateTime occurredAt
    )
    {
        SystemMessageModel message = messageFactory.createLoginSuccessMessage(
                userId,
                username,
                sessionId,
                occurredAt
        );
        dispatcher.dispatch(message);
    }

    @Override
    public void sendLogoutSuccessMessage(
            UUID userId,
            String username,
            UUID sessionId,
            OffsetDateTime occurredAt
    )
    {
        SystemMessageModel message = messageFactory.createLogoutSuccessMessage(
                userId,
                username,
                sessionId,
                occurredAt
        );
        dispatcher.dispatch(message);
    }

    @Override
    public void send(SystemMessageModel message)
    {
        dispatcher.dispatch(Objects.requireNonNull(message, "message must not be null"));
    }

    @Override
    public void sendPasswordResetSuccessMessage(
            UUID userId,
            String email,
            OffsetDateTime occurredAt
    )
    {
        SystemMessageModel message = messageFactory.createPasswordResetSuccessMessage(
                userId,
                email,
                occurredAt
        );
        dispatcher.dispatch(message);
    }
}
