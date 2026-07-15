package Testing;

import Shared.Models.User.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import logic_core.app.dto.request.BlockUserRequest;
import logic_core.app.dto.response.BlockActionResponse;
import logic_core.app.dto.validator.BlockValidator;
import logic_core.app.security.CurrentUserProvider;
import logic_core.app.usecase.relation.BlockUserUseCase;
import logic_core.common.exception.OperationNotAllowedException;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.DomainEvent;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.Relationship.UserBlockedEvent;
import logic_core.domain.policy.BlockPolicy;
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

class BlockUserUseCaseTest
{
    private EntityManager entityManager;

    private BlockValidator validator;
    private BlockPolicy policy;
    private RelationshipRepository relationshipRepository;
    private UserRepository userRepository;

    private CapturingEventPublisher eventPublisher;
    private FixedCurrentUserProvider currentUserProvider;
    private FixedTimeProvider timeProvider;

    private BlockUserUseCase useCase;

    private UUID blockerId;
    private UUID blockedId;
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
                followDao,
                blockDao,
                muteDao,
                likeDao,
                entityManager
        );

        validator = new BlockValidator();
        policy = new BlockPolicy(userRepository, relationshipRepository);

        eventPublisher = new CapturingEventPublisher();
        timeProvider = new FixedTimeProvider(fixedNow);

        seedUsers();
        entityManager.flush();

        currentUserProvider = new FixedCurrentUserProvider(blockerId);

        useCase = new BlockUserUseCase(
                validator,
                policy,
                relationshipRepository,
                eventPublisher,
                timeProvider,
                currentUserProvider
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
    void execute_whenRequestIsValid_shouldPersistBlockRelation_andPublishEvent()
    {
        BlockUserRequest request = new BlockUserRequest(blockedId);

        assertFalse(relationshipRepository.existsBlockRelation(blockerId, blockedId));
        assertFalse(relationshipRepository.isBlockedBy(blockerId, blockedId));
        assertEquals(0L, countBlockRows(blockerId, blockedId));

        Result<BlockActionResponse> result = useCase.execute(request);

        assertTrue(result.isSuccess());
        BlockActionResponse response = result.getData();
        assertNotNull(response);

        entityManager.flush();
        entityManager.clear();

        assertTrue(relationshipRepository.existsBlockRelation(blockerId, blockedId));
        assertTrue(relationshipRepository.isBlockedBy(blockerId, blockedId));
        assertEquals(1L, countBlockRows(blockerId, blockedId));

        assertEquals(1, eventPublisher.publishedEvents.size());
        assertTrue(eventPublisher.publishedEvents.get(0) instanceof UserBlockedEvent);

        UserBlockedEvent event = (UserBlockedEvent) eventPublisher.publishedEvents.get(0);
        assertEquals(blockerId, event.getBlockerId());
        assertEquals(blockedId, event.getBlockedId());
        assertEquals(fixedNow, event.getOccurredAt());
    }

    @Test
    void execute_whenUserTriesToBlockSelf_shouldThrowValidationException_andNotChangeDb()
    {
        BlockUserRequest request = new BlockUserRequest(blockerId);

        assertThrows(
                logic_core.common.exception.ValidationException.class,
                () -> useCase.execute(request)
        );

        entityManager.flush();
        entityManager.clear();

        assertFalse(relationshipRepository.existsBlockRelation(blockerId, blockerId));
        assertEquals(0L, countBlockRows(blockerId, blockerId));
        assertTrue(eventPublisher.publishedEvents.isEmpty());
    }

    @Test
    void execute_whenAlreadyBlocked_shouldFail_andNotCreateDuplicateRows()
    {
        BlockUserRequest request = new BlockUserRequest(blockedId);

        Result<BlockActionResponse> first = useCase.execute(request);
        assertTrue(first.isSuccess());

        entityManager.flush();
        entityManager.clear();

        long rowsAfterFirst = countBlockRows(blockerId, blockedId);
        assertEquals(1L, rowsAfterFirst);

        Result<BlockActionResponse> second = useCase.execute(request);
        assertFalse(second.isSuccess());
        assertEquals("Block relation already exists.", second.getError());

        entityManager.flush();
        entityManager.clear();

        long rowsAfterSecond = countBlockRows(blockerId, blockedId);
        assertEquals(1L, rowsAfterSecond, "A duplicated row should not be created.");

        assertTrue(relationshipRepository.existsBlockRelation(blockerId, blockedId));
        assertTrue(relationshipRepository.isBlockedBy(blockerId, blockedId));

        assertEquals(1, eventPublisher.publishedEvents.size(),
                "Only the first successful block should publish an event.");
    }

    @Test
    void execute_whenPolicyRejects_shouldReturnFailure_andNotPersist_orPublish()
    {
        BlockPolicy rejectingPolicy = new BlockPolicy(userRepository, relationshipRepository)
        {
            @Override
            public void validateBlock(UUID actorId, UUID targetId)
            {
                throw new OperationNotAllowedException("Block is not allowed.");
            }
        };

        useCase = new BlockUserUseCase(
                validator,
                rejectingPolicy,
                relationshipRepository,
                eventPublisher,
                timeProvider,
                currentUserProvider
        );

        BlockUserRequest request = new BlockUserRequest(blockedId);

        long rowsBefore = countBlockRows(blockerId, blockedId);

        Result<BlockActionResponse> result = useCase.execute(request);

        assertFalse(result.isSuccess());
        assertEquals("Block is not allowed.", result.getError());

        entityManager.flush();
        entityManager.clear();

        long rowsAfter = countBlockRows(blockerId, blockedId);
        assertEquals(rowsBefore, rowsAfter);

        assertFalse(relationshipRepository.existsBlockRelation(blockerId, blockedId));
        assertFalse(relationshipRepository.isBlockedBy(blockerId, blockedId));
        assertTrue(eventPublisher.publishedEvents.isEmpty());
    }

    // =========================================================
    //                         HELPERS
    // =========================================================

    private void seedUsers()
    {
        User blocker = User.builder()
                .username("blocker_" + UUID.randomUUID())
                .email("blocker_" + UUID.randomUUID() + "@test.com")
                .passwordHash("hash")
                .displayName("Blocker")
                .build();

        User blocked = User.builder()
                .username("blocked_" + UUID.randomUUID())
                .email("blocked_" + UUID.randomUUID() + "@test.com")
                .passwordHash("hash")
                .displayName("Blocked")
                .build();

        entityManager.persist(blocker);
        entityManager.persist(blocked);
        entityManager.flush();

        this.blockerId = blocker.getId();
        this.blockedId = blocked.getId();
    }

    private long countBlockRows(UUID blockerId, UUID blockedId)
    {
        return entityManager.createQuery("""
            select count(b)
            from Block b
            where b.blocker.id = :blockerId
              and b.blocked.id = :blockedId
            """, Long.class)
                .setParameter("blockerId", blockerId)
                .setParameter("blockedId", blockedId)
                .getSingleResult();
    }

    private static class FixedCurrentUserProvider extends CurrentUserProvider
    {
        private final UUID currentUserId;

        private FixedCurrentUserProvider(UUID currentUserId)
        {
            this.currentUserId = currentUserId;
        }

        @Override
        public UUID requireCurrentUserId()
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

        @Override
        public OffsetDateTime now()
        {
            return now;
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
