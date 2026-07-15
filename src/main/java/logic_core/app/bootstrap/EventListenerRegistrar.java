package logic_core.app.bootstrap;

import logic_core.app.listener.authentication.UserLoggedInSystemMessageListener;
import logic_core.app.listener.authentication.UserLoggedOutSystemMessageListener;
import logic_core.app.listener.authentication.UserRegisteredSystemMessageListener;
import logic_core.app.systemMessage.SystemMessageService;
import logic_core.domain.event.EventBus;
import logic_core.domain.event.authentication.UserLoggedInEvent;
import logic_core.domain.event.authentication.UserLoggedOutEvent;
import logic_core.domain.event.userEvent.UserRegisteredEvent;

public final class EventListenerRegistrar
{
    private EventListenerRegistrar()
    {
    }

    public static void registerSystemMessageListeners(
            EventBus eventBus,
            SystemMessageService systemMessageService
    )
    {
        eventBus.register(
                UserRegisteredEvent.class,
                new UserRegisteredSystemMessageListener(systemMessageService)
        );

        eventBus.register(
                UserLoggedInEvent.class,
                new UserLoggedInSystemMessageListener(systemMessageService)
        );

        eventBus.register(
                UserLoggedOutEvent.class,
                new UserLoggedOutSystemMessageListener(systemMessageService)
        );
    }
}
