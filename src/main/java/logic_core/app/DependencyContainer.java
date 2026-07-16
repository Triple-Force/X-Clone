package logic_core.app;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import logic_core.app.bootstrap.EventListenerRegistrar;
import logic_core.app.dto.validator.LoginValidator;
import logic_core.app.dto.validator.RefreshSessionValidator;
import logic_core.app.dto.validator.RegisterValidator;
import logic_core.app.facade.AuthFacade;
import logic_core.app.systemMessage.DefaultSystemMessageFactory;
import logic_core.app.systemMessage.LoggingSystemMessageDispatcher;
import logic_core.app.systemMessage.SystemMessageService;
import logic_core.app.systemMessage.SystemMessageServiceImpl;
import logic_core.app.usecase.auth.LoginUserUseCase;
import logic_core.app.usecase.auth.LogoutUserUseCase;
import logic_core.app.usecase.auth.RefreshSessionUseCase;
import logic_core.app.usecase.auth.RegisterUserUseCase;
import logic_core.common.security.PasswordHasher;
import logic_core.common.security.TokenGenerator;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventBus;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.policy.RegistrationPolicy;
import logic_core.domain.policy.SessionPolicy;
import logic_core.domain.repository.SessionRepository;
import logic_core.domain.repository.UserRepository;
import logic_core.infrastructure.dao.SessionDao;
import logic_core.infrastructure.dao.UserDao;
import logic_core.infrastructure.event.AsyncEventBus;
import logic_core.infrastructure.repository.JpaSessionRepository;
import logic_core.infrastructure.repository.JpaUserRepository;
import logic_core.session.SessionFactory;
import logic_core.session.SessionManager;
import lombok.Getter;

/**
 * Composition root.

 * Lifecycle rules:
 * - EntityManagerFactory: app-scoped
 * - EventBus + system message listeners: app-scoped (singleton)
 * - EntityManager / DAOs / repositories / use cases / facade: request-scoped
 */
public final class DependencyContainer
{
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("X-Clone-PU");

    /**
     * Shared async event bus for the whole application process.
     * Must NOT be created per request; otherwise registered listeners are lost.
     */
    @Getter
    private static final EventBus eventBus = createAndWireEventBus();

    private DependencyContainer()
    {
    }

    // -------------------------------------------------------------------------
    // App-scoped wiring
    // -------------------------------------------------------------------------

    private static EventBus createAndWireEventBus()
    {
        EventBus bus = new AsyncEventBus();

        SystemMessageService systemMessageService = new SystemMessageServiceImpl(
                new DefaultSystemMessageFactory(),
                new LoggingSystemMessageDispatcher()
        );

        // One-time registration of system-message listeners
        EventListenerRegistrar.registerSystemMessageListeners(
                bus,
                systemMessageService
        );

        return bus;
    }

    public static EventPublisher getEventPublisher()
    {
        return eventBus;
    }

    // -------------------------------------------------------------------------
    // Request-scoped factories
    // -------------------------------------------------------------------------

    public static EntityManager createEntityManager()
    {
        return emf.createEntityManager();
    }

    /**
     * Builds a request-scoped AuthFacade.
     * Reuses the shared EventBus instance so published events hit registered listeners.
     */
    public static AuthFacade createAuthFacade(EntityManager em)
    {
        TimeProvider timeProvider = new TimeProvider();
        PasswordHasher passwordHasher = new PasswordHasher();
        TokenGenerator tokenGenerator = new TokenGenerator();

        EventPublisher eventPublisher = eventBus;

        UserDao userDao = new UserDao(em);
        SessionDao sessionDao = new SessionDao(em, timeProvider);

        UserRepository userRepository = new JpaUserRepository(userDao);
        SessionRepository sessionRepository = new JpaSessionRepository(sessionDao,em);

        RegistrationPolicy registrationPolicy = new RegistrationPolicy(userRepository);
        SessionPolicy sessionPolicy = new SessionPolicy(sessionRepository, userRepository);

        SessionFactory sessionFactory = new SessionFactory(
                tokenGenerator,
                timeProvider
        );

        SessionManager sessionManager = new SessionManager(
               sessionDao,
                userDao,
                sessionFactory,
                timeProvider
        );

        RegisterValidator registerValidator = new RegisterValidator();
        LoginValidator loginValidator = new LoginValidator();
        RefreshSessionValidator refreshSessionValidator = new RefreshSessionValidator();

        RegisterUserUseCase registerUserUseCase = new RegisterUserUseCase(
                registerValidator,
                registrationPolicy,
                userRepository,
                eventPublisher,
                passwordHasher,
                timeProvider,
                sessionManager
        );

        LoginUserUseCase loginUserUseCase = new LoginUserUseCase(
                userRepository,
                loginValidator,
                passwordHasher,
                sessionManager,
                eventPublisher,
                timeProvider
        );

        LogoutUserUseCase logoutUserUseCase = new LogoutUserUseCase(
                sessionManager,
                eventPublisher,
                timeProvider,
                userRepository
        );

        RefreshSessionUseCase refreshSessionUseCase = new RefreshSessionUseCase(
                refreshSessionValidator,
                sessionPolicy,
                sessionManager,
                eventPublisher,
                timeProvider,
                userRepository
        );

        return new AuthFacade(
                registerUserUseCase,
                loginUserUseCase,
                logoutUserUseCase,
                refreshSessionUseCase
        );
    }

    // -------------------------------------------------------------------------
    // Shutdown
    // -------------------------------------------------------------------------

    public static void shutdown()
    {
        try
        {
            eventBus.shutdown();
        } catch (Exception ignored)
        {
            // keep shutdown resilient
        }

        if (emf != null && emf.isOpen())
        {
            emf.close();
        }
    }
}
