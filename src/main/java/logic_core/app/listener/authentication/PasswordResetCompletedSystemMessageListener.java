package logic_core.app.listener.authentication;

import logic_core.app.systemMessage.SystemMessageService;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventListener;
import logic_core.domain.event.authentication.PasswordResetCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener for PasswordResetCompletedEvent to trigger security notifications.
 * Follows the project's event-driven side-effect pattern.
 */
@RequiredArgsConstructor
public class PasswordResetCompletedSystemMessageListener implements EventListener<PasswordResetCompletedEvent>
{
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetCompletedSystemMessageListener.class);
    private final SystemMessageService systemMessageService;
    private static final TimeProvider timeProvider = new TimeProvider();

    @Override
    public void onEvent(PasswordResetCompletedEvent event)
    {
        logger.info("Handling PasswordResetCompletedEvent for User ID: {}", event.getUserId());

        systemMessageService.sendPasswordResetSuccessMessage(
                event.getUserId(),
                event.getEmail(),
                timeProvider.now()
        );
    }
}
