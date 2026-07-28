package logic_core.session;

import Shared.Models.Session.Session;
import Shared.Models.User.User;
import jakarta.persistence.EntityManager;
import logic_core.common.security.TokenGenerator;
import logic_core.common.util.TimeProvider;
import logic_core.domain.model.UserModel;
import logic_core.infrastructure.mapper.UserPersistenceMapper;

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

    public Session create(UserModel userModel, EntityManager entityManager)
    {
        OffsetDateTime now = timeProvider.now();

        User userRef = entityManager.getReference(User.class, userModel.getId());

        return Session.builder()
                .user(userRef)
                .token(tokenGenerator.generateToken())
                .expiresAt(now.plusDays(DEFAULT_EXPIRATION_DAYS))
                .build();
    }
}
