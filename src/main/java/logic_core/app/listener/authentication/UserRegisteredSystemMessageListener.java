package logic_core.app.listener.authentication;

import logic_core.app.systemMessage.SystemMessageService;
import logic_core.domain.event.EventListener;
import logic_core.domain.event.userEvent.UserRegisteredEvent;

import java.util.Objects;

public class UserRegisteredSystemMessageListener
        implements EventListener<UserRegisteredEvent>
{
    private final SystemMessageService systemMessageService;

    public UserRegisteredSystemMessageListener(SystemMessageService systemMessageService)
    {
        this.systemMessageService = Objects.requireNonNull(
                systemMessageService,
                "systemMessageService must not be null"
        );
    }

    @Override
    public void onEvent(UserRegisteredEvent event)
    {
        Objects.requireNonNull(event, "event must not be null");

        systemMessageService.sendWelcomeMessage(
                event.getUserId(),
                event.getUsername(),
                event.getEmail(),
                event.getOccurredAt()
        );
    }
}
