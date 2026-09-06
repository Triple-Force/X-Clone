package logic_core.session;

import logic_core.common.security.TokenGenerator;
import logic_core.common.util.TimeProvider;
import logic_core.domain.model.SessionModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SessionFactory
{
    private static final long DEFAULT_EXPIRATION_DAYS = 30;

    private final TokenGenerator tokenGenerator;
    private final TimeProvider timeProvider;

    public SessionModel create(UUID userId)
    {
        return SessionModel.builder()
                .userId(userId)
                .token(tokenGenerator.generateToken())
                .expiresAt(
                        timeProvider.now().plusDays(DEFAULT_EXPIRATION_DAYS)
                )
                .build();
    }
}
