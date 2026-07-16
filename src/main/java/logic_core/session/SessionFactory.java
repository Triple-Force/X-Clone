package logic_core.session;

import Shared.Models.Session.Session;
import Shared.Models.User.User;
import logic_core.common.security.TokenGenerator;
import logic_core.common.util.TimeProvider;

import java.time.OffsetDateTime;

public class SessionFactory
{
    private static final long DEFAULT_EXPIRATION_DAYS = 30;
    private final TokenGenerator tokenGenerator;
    private final TimeProvider timeProvider;

    public SessionFactory(TokenGenerator tokenGenerator, TimeProvider timeProvider)
    {
        this.tokenGenerator = tokenGenerator;
        this.timeProvider = timeProvider;
    }

    public Session create(User user)
    {
        OffsetDateTime now = timeProvider.now();

        return Session.builder()
                .user(user)
                .token(tokenGenerator.generateToken())
                .expiresAt(now.plusDays(DEFAULT_EXPIRATION_DAYS))
                .build();
    }
}
