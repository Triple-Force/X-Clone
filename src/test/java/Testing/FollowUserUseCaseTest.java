package Testing;

import Shared.Models.User.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import logic_core.app.dto.request.FollowUserRequest;
import logic_core.app.dto.response.FollowResponse;
import logic_core.app.dto.validator.FollowValidator;
import logic_core.app.security.CurrentUserProvider;
import logic_core.app.usecase.relation.FollowUserUseCase;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.DomainEvent;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.Relationship.UserFollowedEvent;
import logic_core.domain.policy.FollowPolicy;
import logic_core.domain.repository.RelationshipRepository;
import logic_core.domain.repository.UserRepository;
import logic_core.infrastructure.dao.*;
import logic_core.infrastructure.repository.JpaRelationshipRepository;
import logic_core.infrastructure.repository.JpaUserRepository;
import org.junit.jupiter.api.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FollowUserUseCaseTest
{
    private EntityManager entityManager;

    private FollowValidator validator;
    private FollowPolicy policy;
    private RelationshipRepository relationshipRepository;
    private UserRepository userRepository;

    private CapturingEventPublisher eventPublisher;
    private FixedCurrentUserProvider currentUserProvider;
    private FixedTimeProvider timeProvider;

    private FollowUserUseCase useCase;

    private UUID followerId;
    private UUID followingId;
    private OffsetDateTime fixedNow;

    private BlockDao blockDao;
    private FollowDao followDao;
    private MuteDao muteDao;
    private LikeDao likeDao;
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

        blockDao = new BlockDao(entityManager);
        followDao = new FollowDao(entityManager);
        muteDao = new MuteDao(entityManager);
        likeDao = new LikeDao(entityManager);
        userDao = new UserDao(entityManager);

        userRepository = new JpaUserRepository(userDao);
        relationshipRepository = new JpaRelationshipRepository(
                followDao, blockDao, muteDao, likeDao, entityManager
        );

        validator = new FollowValidator();
        policy = new FollowPolicy(userRepository, relationshipRepository);

        eventPublisher = new CapturingEventPublisher();
        timeProvider = new FixedTimeProvider(fixedNow);

        seedUsers();
        entityManager.flush();

        currentUserProvider = new FixedCurrentUserProvider(followerId);

        useCase = new FollowUserUseCase(
                validator,
                policy,
                relationshipRepository,
                eventPublisher,
                currentUserProvider,
                timeProvider
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
    void execute_whenRequestIsValid_shouldPersistFollowRelationUpdateFollowerCountAndPublishEvent()
    {
        FollowUserRequest request = new FollowUserRequest(followingId);

        long followersBefore = relationshipRepository.countFollowers(followingId);
        assertEquals(0L, followersBefore);

        Result<FollowResponse> result = useCase.execute(request);

        assertTrue(result.isSuccess());
        FollowResponse response = result.getData();
        assertNotNull(response);
        assertTrue(response.following());
        assertEquals(1L, response.followersCount());

        entityManager.flush();
        entityManager.clear();

        long followersAfter = relationshipRepository.countFollowers(followingId);
        assertEquals(1L, followersAfter);

        assertTrue(relationshipRepository.existsFollowRelation(followerId, followingId));

        assertEquals(1, eventPublisher.publishedEvents.size());
        assertTrue(eventPublisher.publishedEvents.get(0) instanceof UserFollowedEvent);

        UserFollowedEvent event = (UserFollowedEvent) eventPublisher.publishedEvents.get(0);
        assertEquals(followerId, event.getActorId());
        assertEquals(followingId, event.getTargetId());
        assertEquals(fixedNow, event.getOccurredAt());
    }

    @Test
    void execute_whenUserTriesToFollowSelf_shouldThrowValidationException_andNotChangeDb()
    {
        FollowUserRequest request = new FollowUserRequest(followerId);

        assertThrows(
                logic_core.common.exception.ValidationException.class,
                () -> useCase.execute(request)
        );

        entityManager.flush();
        entityManager.clear();

        assertFalse(relationshipRepository.existsFollowRelation(followerId, followerId));
        assertTrue(eventPublisher.publishedEvents.isEmpty());
    }

    @Test
    void execute_whenAlreadyFollowing_shouldFail_andNotCreateDuplicateRows()
    {
        FollowUserRequest request = new FollowUserRequest(followingId);

        Result<FollowResponse> first = useCase.execute(request);
        assertTrue(first.isSuccess());

        entityManager.flush();
        entityManager.clear();

        long rowsAfterFirst = countFollowRows(followerId, followingId);
        assertEquals(1L, rowsAfterFirst);

        Result<FollowResponse> second = useCase.execute(request);
        assertFalse(second.isSuccess());

        entityManager.flush();
        entityManager.clear();

        long rowsAfterSecond = countFollowRows(followerId, followingId);
        assertEquals(1L, rowsAfterSecond, "A duplicated row should not be created.");

        long followers = relationshipRepository.countFollowers(followingId);
        assertEquals(1L, followers);
    }

    @Test
    void execute_whenPolicyRejects_shouldReturnFailure_andNotPersist_orPublish()
    {
        FollowPolicy rejectingPolicy = new FollowPolicy(userRepository, relationshipRepository)
        {
            @Override
            public void validateFollow(UUID followerId, UUID followingId)
            {
                throw new logic_core.common.exception.OperationNotAllowedException("Follow is not allowed.");
            }
        };

        useCase = new FollowUserUseCase(
                validator,
                rejectingPolicy,
                relationshipRepository,
                eventPublisher,
                currentUserProvider,
                timeProvider
        );

        FollowUserRequest request = new FollowUserRequest(followingId);

        long followersBefore = relationshipRepository.countFollowers(followingId);

        Result<FollowResponse> result = useCase.execute(request);

        assertFalse(result.isSuccess());
        assertEquals("Follow is not allowed.", result.getError());

        entityManager.flush();
        entityManager.clear();

        long followersAfter = relationshipRepository.countFollowers(followingId);
        assertEquals(followersBefore, followersAfter);

        assertFalse(relationshipRepository.existsFollowRelation(followerId, followingId));
        assertTrue(eventPublisher.publishedEvents.isEmpty());
    }

    // =========================================================
    //                         HELPERS
    // =========================================================
    private void seedUsers()
    {
        User follower = User.builder()
                .username("follower_" + UUID.randomUUID())
                .email("f_" + UUID.randomUUID() + "@test.com")
                .passwordHash("hash")
                .displayName("Follower")
                .build();

        User following = User.builder()
                .username("following_" + UUID.randomUUID())
                .email("t_" + UUID.randomUUID() + "@test.com")
                .passwordHash("hash")
                .displayName("Target")
                .build();

        entityManager.persist(follower);
        entityManager.persist(following);
        entityManager.flush();

        this.followerId = follower.getId();
        this.followingId = following.getId();
    }

    private long countFollowRows(UUID followerId, UUID followingId)
    {
        return entityManager.createQuery("""
            select count(f)
            from Follow f
            where f.follower.id = :followerId
              and f.following.id = :followingId
            """, Long.class)
                .setParameter("followerId", followerId)
                .setParameter("followingId", followingId)
                .getSingleResult();
    }

    private static class FixedCurrentUserProvider extends CurrentUserProvider
    {
        private final UUID currentUserId;
        private FixedCurrentUserProvider(UUID currentUserId)
        {
            this.currentUserId = currentUserId;
        }
        @Override public UUID requireCurrentUserId()
        {
            return currentUserId;
        }
    }

    private static class FixedTimeProvider extends TimeProvider
    {
        private final OffsetDateTime now;
        private FixedTimeProvider(OffsetDateTime now)
        {
            this.now = now;
        }
        @Override public OffsetDateTime now()
        {
            return now;
        }
    }

    private static class CapturingEventPublisher implements EventPublisher
    {
        private final List<DomainEvent> publishedEvents = new ArrayList<>();
        @Override public void publish(DomainEvent event)
        {
            publishedEvents.add(event);
        }
    }
}
