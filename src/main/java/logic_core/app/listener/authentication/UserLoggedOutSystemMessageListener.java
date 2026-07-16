package logic_core.app.listener.authentication;

import logic_core.app.systemMessage.SystemMessageService;
import logic_core.domain.event.EventListener;
import logic_core.domain.event.authentication.UserLoggedOutEvent;

import java.util.Objects;

public class UserLoggedOutSystemMessageListener
        implements EventListener<UserLoggedOutEvent>
{
    private final SystemMessageService systemMessageService;

    public UserLoggedOutSystemMessageListener(SystemMessageService systemMessageService)
    {
        this.systemMessageService = Objects.requireNonNull(
                systemMessageService,
                "systemMessageService must not be null"
        );
    }

    @Override
    public void onEvent(UserLoggedOutEvent event)
    {
        Objects.requireNonNull(event, "event must not be null");

        systemMessageService.sendLogoutSuccessMessage(
                event.getUserId(),
                event.getUsername(),
                event.getSessionId(),
                event.getOccurredAt()
        );
    }
}
