package logic_core.app.listener.authentication;

import logic_core.app.systemMessage.SystemMessageService;
import logic_core.domain.event.EventListener;
import logic_core.domain.event.authentication.UserLoggedInEvent;

import java.util.Objects;

public class UserLoggedInSystemMessageListener
        implements EventListener<UserLoggedInEvent>
{
    private final SystemMessageService systemMessageService;

    public UserLoggedInSystemMessageListener(SystemMessageService systemMessageService)
    {
        this.systemMessageService = Objects.requireNonNull(
                systemMessageService,
                "systemMessageService must not be null"
        );
    }

    @Override
    public void onEvent(UserLoggedInEvent event)
    {
        Objects.requireNonNull(event, "event must not be null");

        systemMessageService.sendLoginSuccessMessage(
                event.getUserId(),
                event.getUsername(),
                event.getSessionId(),
                event.getOccurredAt()
        );
    }
}
