package Testing;

import Shared.Models.Session.Session;
import Shared.Models.User.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import logic_core.app.dto.request.RefreshSessionRequest;
import logic_core.app.dto.response.AuthResponse;
import logic_core.app.dto.validator.RefreshSessionValidator;
import logic_core.app.usecase.auth.RefreshSessionUseCase;
import logic_core.common.result.Result;
import logic_core.common.security.TokenGenerator;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.authentication.SessionRefreshedEvent;
import logic_core.domain.event.DomainEvent;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.policy.SessionPolicy;
import logic_core.domain.repository.SessionRepository;
import logic_core.domain.repository.UserRepository;
import logic_core.infrastructure.dao.SessionDao;
import logic_core.infrastructure.dao.UserDao;
import logic_core.infrastructure.repository.JpaSessionRepository;
import logic_core.infrastructure.repository.JpaUserRepository;
import logic_core.session.SessionFactory;
import logic_core.session.SessionManager;
import org.junit.jupiter.api.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RefreshSessionUseCaseTest
{
    private EntityManager entityManager;
    private SessionFactory sessionFactory;
    private TokenGenerator tokenGenerator;

    private RefreshSessionValidator validator;
    private SessionPolicy policy;
    private SessionRepository sessionRepository;
    private UserRepository userRepository;
    private SessionManager sessionManager;

    private CapturingEventPublisher eventPublisher;
    private FixedTimeProvider timeProvider;

    private RefreshSessionUseCase useCase;

    private User testUser;
    private Session testSession;
    private String validRefreshToken;
    private OffsetDateTime fixedNow;

    private SessionDao sessionDao;
    private UserDao userDao;

    private static EntityManagerFactory emf;

    @BeforeAll
    static void beforeAll()
    {
        emf = Persistence.createEntityManagerFactory("X-Clone-PU");
    }

    @AfterAll
    static void afterAll()
    {
        if (emf != null && emf.isOpen())
        {
            emf.close();
        }
    }

    @BeforeEach
    void setUp()
    {
        entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();

        fixedNow = OffsetDateTime.now();

        sessionDao = new SessionDao(entityManager, timeProvider);
        userDao = new UserDao(entityManager);

        tokenGenerator = new TokenGenerator();
        sessionFactory = new SessionFactory(tokenGenerator,timeProvider);

        userRepository = new JpaUserRepository(userDao);
        sessionRepository = new JpaSessionRepository(sessionDao, entityManager);


        sessionManager = new SessionManager(sessionDao, userDao,sessionFactory, timeProvider)
        {
            @Override
            public Session startSession(UUID userid)
            {
                User userEntity = entityManager.find(User.class, userid);
                Session newSession = new Session();
                newSession.setUser(userEntity);
                newSession.setToken("new-token-" + UUID.randomUUID());
                newSession.setExpiresAt(fixedNow.plusDays(1));
                sessionRepository.save(newSession);
                return newSession;
            }
        };

        validator = new RefreshSessionValidator() {
            @Override
            public void validate(String token) {
                if (token == null || token.isBlank())
                {
                    throw new IllegalArgumentException("Token cannot be blank.");
                }
            }
        };

        policy = new SessionPolicy(sessionRepository, userRepository);
        eventPublisher = new CapturingEventPublisher();
        timeProvider = new FixedTimeProvider(fixedNow);

        seedData();
        entityManager.flush();

        useCase = new RefreshSessionUseCase(
                validator,
                policy,
                sessionManager,
                eventPublisher,
                timeProvider,
                userRepository
        );
    }

    @AfterEach
    void tearDown()
    {
        try
        {
            if (entityManager != null && entityManager.getTransaction().isActive())
            {
                entityManager.getTransaction().rollback();
            }
        }
        finally
        {
            if (entityManager != null && entityManager.isOpen())
            {
                entityManager.close();
            }
        }
    }

    // =========================================================
    //                         TESTS
    // =========================================================

    @Test
    void execute_whenRequestIsValid_shouldCreateNewSession_invalidateOldSession_andPublishEvent()
    {
        RefreshSessionRequest request = new RefreshSessionRequest(validRefreshToken);

        assertTrue(sessionRepository.findByToken(validRefreshToken).isPresent());

        Result<AuthResponse> result = useCase.execute(request);

        assertTrue(result.isSuccess());
        AuthResponse response = result.getData();
        assertNotNull(response);

        entityManager.flush();
        entityManager.clear();

        assertFalse(sessionRepository.findByToken(validRefreshToken).isPresent());

        assertNotNull(response.token());
        assertTrue(sessionRepository.findByToken(response.token()).isPresent());

        assertEquals(1, eventPublisher.publishedEvents.size());
        assertTrue(eventPublisher.publishedEvents.get(0) instanceof SessionRefreshedEvent);

        SessionRefreshedEvent event = (SessionRefreshedEvent) eventPublisher.publishedEvents.get(0);
        assertEquals(testUser.getId(), event.getUserId());
        assertEquals(testSession.getId(), event.getOldSessionId());
        assertNotNull(event.getNewSessionId());
        assertEquals(fixedNow, event.getOccurredAt());
    }

    @Test
    void execute_whenTokenIsInvalidByValidator_shouldReturnFailure()
    {
        RefreshSessionRequest request = new RefreshSessionRequest("");

        Result<AuthResponse> result = useCase.execute(request);

        assertFalse(result.isSuccess());
        assertEquals("Token cannot be blank.", result.getError());
        assertTrue(eventPublisher.publishedEvents.isEmpty());
    }

    @Test
    void execute_whenSessionNotFoundInDb_shouldReturnFailure()
    {
        RefreshSessionRequest request = new RefreshSessionRequest("non-existent-token");

        Result<AuthResponse> result = useCase.execute(request);

        assertFalse(result.isSuccess());
        assertEquals("Session not found for provided token.", result.getError());
        assertTrue(eventPublisher.publishedEvents.isEmpty());
    }

    @Test
    void execute_whenPolicyFailsDueToExpiredSession_shouldReturnFailure_andNotChangeSessionState()
    {
        timeProvider = new FixedTimeProvider(fixedNow.plusDays(5));
        useCase = new RefreshSessionUseCase(
                validator,
                policy,
                sessionManager,
                eventPublisher,
                timeProvider,
                userRepository
        );

        RefreshSessionRequest request = new RefreshSessionRequest(validRefreshToken);

        Result<AuthResponse> result = useCase.execute(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("expired"));

        entityManager.flush();
        entityManager.clear();

        assertTrue(sessionRepository.findByToken(validRefreshToken).isPresent());
        assertTrue(eventPublisher.publishedEvents.isEmpty());
    }

    @Test
    void execute_whenUserIsInactive_shouldReturnFailure_viaPolicy()
    {
        testUser.setActive(false);
        entityManager.merge(testUser);
        entityManager.flush();
        entityManager.clear();

        RefreshSessionRequest request = new RefreshSessionRequest(validRefreshToken);

        Result<AuthResponse> result = useCase.execute(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("inactive") || result.getError().contains("inActive"));

        entityManager.flush();
        entityManager.clear();

        assertTrue(sessionRepository.findByToken(validRefreshToken).isPresent());
        assertTrue(eventPublisher.publishedEvents.isEmpty());
    }

    // =========================================================
    //                         HELPERS
    // =========================================================

    private void seedData()
    {
        testUser = User.builder()
                .username("test_user_" + UUID.randomUUID())
                .email("user_" + UUID.randomUUID() + "@test.com")
                .passwordHash("hash123")
                .displayName("Test User")
                .build();
        testUser.setActive(true);

        entityManager.persist(testUser);

        validRefreshToken = "valid-refresh-token-" + UUID.randomUUID();

        testSession = new Session();
        testSession.setUser(testUser);
        testSession.setToken(validRefreshToken);
        testSession.setExpiresAt(fixedNow.plusDays(2));

        entityManager.persist(testSession);
        entityManager.flush();
    }

    private static class FixedTimeProvider extends TimeProvider
    {
        private OffsetDateTime now;

        private FixedTimeProvider(OffsetDateTime now)
        {
            this.now = now;
        }

        @Override
        public OffsetDateTime now()
        {
            return now;
        }

        public void setNow(OffsetDateTime now)
        {
            this.now = now;
        }
    }

    private static class CapturingEventPublisher implements EventPublisher
    {
        private final List<DomainEvent> publishedEvents = new ArrayList<>();

        @Override
        public void publish(DomainEvent event)
        {
            publishedEvents.add(event);
        }
    }
}
