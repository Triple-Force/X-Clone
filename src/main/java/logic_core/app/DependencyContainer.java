package logic_core.app;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import logic_core.app.bootstrap.EventListenerRegistrar;
import logic_core.app.dto.response.GetConversationsResponse;
import logic_core.app.dto.validator.*;
import logic_core.app.facade.*;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.CurrentAuthContext;
import logic_core.app.service.passwordReset.LoggingPasswordResetDeliveryAdapter;
import logic_core.app.service.passwordReset.PasswordResetDeliveryPort;
import logic_core.app.service.passwordReset.PasswordResetOtpService;
import logic_core.app.systemMessage.DefaultSystemMessageFactory;
import logic_core.app.systemMessage.LoggingSystemMessageDispatcher;
import logic_core.app.systemMessage.SystemMessageService;
import logic_core.app.systemMessage.SystemMessageServiceImpl;
import logic_core.app.usecase.User.*;
import logic_core.app.usecase.auth.*;
import logic_core.app.usecase.conversation.*;
import logic_core.app.usecase.message.*;
import logic_core.app.usecase.relation.*;
import logic_core.app.usecase.timeline.GetTimelineUseCase;
import logic_core.app.usecase.tweet.*;
import logic_core.common.security.PasswordHasher;
import logic_core.common.security.TokenGenerator;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventBus;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.policy.*;
import logic_core.domain.repository.*;
import logic_core.domain.service.LocalMediaStorageService;
import logic_core.domain.service.MediaStorageService;
import logic_core.infrastructure.dao.*;
import logic_core.infrastructure.event.AsyncEventBus;
import logic_core.infrastructure.media.MediaProperties;
import logic_core.infrastructure.repository.*;
import logic_core.session.SessionFactory;
import logic_core.session.SessionManager;
import lombok.Getter;
import lombok.NonNull;


public final class DependencyContainer
{
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("X-Clone-PU");

    private static final TimeProvider timeProvider = new TimeProvider();
    private static final PasswordHasher passwordHasher = new PasswordHasher();

    @Getter
    private static final EventBus eventBus = createAndWireEventBus();

    @Getter
    private static final PasswordResetOtpService passwordResetOtpService =
            new PasswordResetOtpService(timeProvider, passwordHasher);

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

    public static EntityManagerFactory entityManagerFactory()
    {
        return emf;
    }

    public static EntityManager createEntityManager()
    {
        return emf.createEntityManager();
    }


    public static AuthFacade createAuthFacade(EntityManager em)
    {
        TimeProvider timeProvider = new TimeProvider();
        PasswordHasher passwordHasher = new PasswordHasher();
        TokenGenerator tokenGenerator = new TokenGenerator();

        EventPublisher eventPublisher = eventBus;

        UserDao userDao = new UserDao();
        SessionDao sessionDao = new SessionDao(timeProvider);

        UserRepository userRepository = new JpaUserRepository(userDao, em);
        SessionRepository sessionRepository = new JpaSessionRepository(sessionDao,em);

        RegistrationPolicy registrationPolicy = new RegistrationPolicy(userRepository);
        SessionPolicy sessionPolicy = new SessionPolicy(sessionRepository, userRepository);

        SessionFactory sessionFactory = new SessionFactory(
                tokenGenerator,
                timeProvider
        );

        SessionManager sessionManager = new SessionManager(
                sessionRepository,
                userRepository,
                sessionFactory,
                em
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
        AuthLockOrchestrator lockOrchestrator = new AuthLockOrchestrator(userRepository, sessionManager);

        LoginUserUseCase loginUserUseCase = new LoginUserUseCase(
                loginValidator,
                passwordHasher,
                sessionManager,
                eventPublisher,
                timeProvider,
                lockOrchestrator
        );

        LogoutUserUseCase logoutUserUseCase = new LogoutUserUseCase(
                sessionManager,
                eventPublisher,
                timeProvider,
                lockOrchestrator
        );



        RefreshSessionUseCase refreshSessionUseCase = new RefreshSessionUseCase(
                refreshSessionValidator,
                sessionPolicy,
                sessionManager,
                eventPublisher,
                timeProvider,
                lockOrchestrator
        );

        // ---- Password Reset wiring ----
        PasswordResetDeliveryPort deliveryPort =
                new LoggingPasswordResetDeliveryAdapter();



        RequestPasswordResetUseCase requestPasswordResetUseCase =
                new RequestPasswordResetUseCase(
                        userRepository,
                        passwordResetOtpService,
                        deliveryPort
                );

        VerifyPasswordResetCodeUseCase verifyPasswordResetCodeUseCase =
                new VerifyPasswordResetCodeUseCase(
                        passwordResetOtpService
                );

        ResetPasswordUseCase resetPasswordUseCase =
                new ResetPasswordUseCase(
                        userRepository,
                        passwordResetOtpService,
                        passwordHasher,
                        sessionManager,
                        timeProvider,
                        eventPublisher
                );

        return new AuthFacade(
                registerUserUseCase,
                loginUserUseCase,
                logoutUserUseCase,
                refreshSessionUseCase,
                requestPasswordResetUseCase,
                verifyPasswordResetCodeUseCase,
                resetPasswordUseCase
        );
    }


    public static RelationFacade createRelationFacade(EntityManager em)
    {
        TimeProvider timeProvider = new TimeProvider();
        TokenGenerator tokenGenerator = new TokenGenerator();

        EventPublisher eventPublisher = eventBus;

        FollowDao followDao = new FollowDao();
        BlockDao blockDao = new BlockDao();
        MuteDao muteDao = new MuteDao();
        LikeDao likeDao = new LikeDao();

        UserDao userDao = new UserDao();

        SessionDao sessionDao = new SessionDao(timeProvider);

        RelationshipRepository relationshipRepository = new JpaRelationshipRepository(
                followDao,
                blockDao,
                muteDao,
                likeDao,
                em
        );

        BlockValidator blockValidator = new BlockValidator();
        FollowValidator followValidator = new FollowValidator();
        MuteValidator muteValidator = new MuteValidator();

        UserRepository userRepository = new JpaUserRepository(userDao, em);

        BlockPolicy blockPolicy = new BlockPolicy(
                userRepository,
                relationshipRepository
        );

        FollowPolicy followPolicy = new FollowPolicy(
                userRepository,
                relationshipRepository
        );

        MutePolicy mutePolicy = new MutePolicy(
                userRepository,
                relationshipRepository
        );


        SessionRepository sessionRepository = new JpaSessionRepository(
                sessionDao,
                em
        );

        SessionFactory sessionFactory = new SessionFactory(tokenGenerator, timeProvider);

        SessionManager sessionManager = new SessionManager(
                sessionRepository,
                userRepository,
                sessionFactory,
                em
        );

        AuthLockOrchestrator lockOrchestrator = new AuthLockOrchestrator(
                userRepository,
                sessionManager
        );

        BlockUserUseCase blockUserUseCase = new BlockUserUseCase(
                blockValidator,
                blockPolicy,
                relationshipRepository,
                eventPublisher,
                timeProvider,
                lockOrchestrator
        );

        FollowUserUseCase followUserUseCase = new FollowUserUseCase(
                followValidator,
                followPolicy,
                relationshipRepository,
                eventPublisher,
                timeProvider,
                lockOrchestrator
        );

        MuteUserUseCase muteUserUseCase = new MuteUserUseCase(
                muteValidator,
                mutePolicy,
                relationshipRepository,
                eventPublisher,
                timeProvider,
                lockOrchestrator
        );

        UnblockUserUseCase unblockUserUseCase = new UnblockUserUseCase(
                blockValidator,
                blockPolicy,
                relationshipRepository,
                eventPublisher,
                timeProvider,
                lockOrchestrator
        );

        UnfollowUserUseCase unfollowUserUseCase = new UnfollowUserUseCase(
                followValidator,
                followPolicy,
                relationshipRepository,
                eventPublisher,
                timeProvider,
                lockOrchestrator
        );

        UnmuteUserUseCase unmuteUserUseCase = new UnmuteUserUseCase(
                muteValidator,
                mutePolicy,
                relationshipRepository,
                eventPublisher,
                timeProvider,
                lockOrchestrator
        );

        return new RelationFacade(
                blockUserUseCase,
                followUserUseCase,
                muteUserUseCase,
                unblockUserUseCase,
                unfollowUserUseCase,
                unmuteUserUseCase
        );
    }


    public static ConversationFacade createConversationFacade(EntityManager em)
    {
        TimeProvider timeProvider = new TimeProvider();
        TokenGenerator tokenGenerator = new TokenGenerator();

        EventPublisher eventPublisher = eventBus;


        ConversationValidator conversationValidator = new ConversationValidator();


        FollowDao followDao = new FollowDao();
        BlockDao blockDao = new BlockDao();
        MuteDao muteDao = new MuteDao();
        LikeDao likeDao = new LikeDao();

        UserDao userDao = new UserDao();
        SessionDao sessionDao = new SessionDao(timeProvider);

        ConversationDao conversationDao = new ConversationDao();
        ConversationMemberDao conversationMemberDao = new ConversationMemberDao();
        DirectMessageDao directMessageDao = new DirectMessageDao();
        RelationshipRepository relationshipRepository = new JpaRelationshipRepository(
                followDao,
                blockDao,
                muteDao,
                likeDao,
                em
        );

        UserRepository userRepository = new JpaUserRepository(userDao, em);

        ConversationRepository conversationRepository = new JpaConversationRepository(
                conversationDao,
                conversationMemberDao,
                em
        );

        ConversationPolicy conversationPolicy = new ConversationPolicy(
                userRepository,
                relationshipRepository,
                conversationRepository
        );




        SessionRepository sessionRepository = new JpaSessionRepository(
                sessionDao,
                em
        );

        DirectMessageRepository directMessageRepository = new JpaDirectMessageRepository(
                directMessageDao,
                em
        );

        SessionFactory sessionFactory = new SessionFactory(tokenGenerator, timeProvider);

        SessionManager sessionManager = new SessionManager(
                sessionRepository,
                userRepository,
                sessionFactory,
                em
        );

        AuthLockOrchestrator lockOrchestrator = new AuthLockOrchestrator(
                userRepository,
                sessionManager
        );

        AddMemberToConversationUseCase addMemberToConversationUseCase = new AddMemberToConversationUseCase(
                conversationValidator,
                conversationPolicy,
                conversationRepository,
                timeProvider,
                eventPublisher,
                lockOrchestrator
        );

        CreateConversationUseCase createConversationUseCase = new CreateConversationUseCase(
                conversationValidator,
                conversationPolicy,
                conversationRepository,
                timeProvider,
                eventPublisher,
                lockOrchestrator
        );

        DeleteConversationUseCase deleteConversationUseCase = new DeleteConversationUseCase(
                conversationValidator,
                conversationPolicy,
                conversationRepository,
                eventPublisher,
                timeProvider,
                lockOrchestrator

        );

        DeleteMemberFromConversationUseCase deleteMemberFromConversationUseCase = new DeleteMemberFromConversationUseCase(
                conversationValidator,
                conversationPolicy,
                conversationRepository,
                eventPublisher,
                timeProvider,
                lockOrchestrator
        );


        GetConversationsUseCase getConversationsUseCase = new GetConversationsUseCase(
                conversationRepository,
                directMessageRepository,
                userRepository,
                lockOrchestrator,
                eventPublisher,
                timeProvider
        );

        return new ConversationFacade(
                addMemberToConversationUseCase,
                createConversationUseCase,
                deleteConversationUseCase,
                deleteMemberFromConversationUseCase,
                getConversationsUseCase
        );
    }

    public static MessageFacade createMessageFacade(EntityManager em)
    {
        TimeProvider timeProvider = new TimeProvider();
        TokenGenerator tokenGenerator = new TokenGenerator();

        EventPublisher eventPublisher = eventBus;

        MessageValidator messageValidator = new MessageValidator();

        DirectMessageDao directMessageDao = new DirectMessageDao();
        ConversationDao conversationDao = new ConversationDao();
        ConversationMemberDao conversationMemberDao = new ConversationMemberDao();
        SessionDao sessionDao = new SessionDao(timeProvider);

        UserDao userDao = new UserDao();

        FollowDao followDao = new FollowDao();
        BlockDao blockDao = new BlockDao();
        MuteDao muteDao = new MuteDao();
        LikeDao likeDao = new LikeDao();

        DirectMessageRepository directMessageRepository = new JpaDirectMessageRepository(
                directMessageDao,
                em
        );

        ConversationRepository conversationRepository = new JpaConversationRepository(
                conversationDao,
                conversationMemberDao,
                em
        );


        RelationshipRepository relationshipRepository = new JpaRelationshipRepository(
                followDao,
                blockDao,
                muteDao,
                likeDao,
                em
        );

        UserRepository userRepository = new JpaUserRepository(userDao, em);

        DirectMessagePolicy directMessagePolicy = new DirectMessagePolicy(
                userRepository,
                relationshipRepository,
                conversationRepository,
                directMessageRepository
        );




        SessionRepository sessionRepository = new JpaSessionRepository(
                sessionDao,
                em
        );

        SessionFactory sessionFactory = new SessionFactory(tokenGenerator, timeProvider);

        SessionManager sessionManager = new SessionManager(
                sessionRepository,
                userRepository,
                sessionFactory,
                em
        );

        AuthLockOrchestrator lockOrchestrator = new AuthLockOrchestrator(
                userRepository,
                sessionManager
        );

        DeleteMessageUseCase deleteMessageUseCase = new DeleteMessageUseCase(
                directMessageRepository,
                messageValidator,
                conversationRepository,
                eventPublisher,
                timeProvider,
                lockOrchestrator
        );

        EditMessageUseCase editMessageUseCase = new EditMessageUseCase(
                messageValidator,
                directMessagePolicy,
                directMessageRepository,
                conversationRepository,
                lockOrchestrator,
                eventPublisher,
                timeProvider
        );

        GetConversationMessagesUseCase getConversationMessagesUseCase = new GetConversationMessagesUseCase(
                messageValidator,
                directMessagePolicy,
                directMessageRepository,
                lockOrchestrator
        );

        GetMessageUseCase getMessageUseCase = new GetMessageUseCase(
                messageValidator,
                directMessagePolicy,
                directMessageRepository,
                lockOrchestrator
        );

        SendMessageUseCase sendMessageUseCase = new SendMessageUseCase(
                directMessageRepository,
                conversationRepository,
                lockOrchestrator,
                eventPublisher,
                timeProvider
        );

        return new MessageFacade(
                deleteMessageUseCase,
                editMessageUseCase,
                getConversationMessagesUseCase,
                getMessageUseCase,
                sendMessageUseCase
        );
    }


    public static TimelineFacade createTimelineFacade(EntityManager em)
    {
        TimeProvider timeProvider = new TimeProvider();

        EventPublisher eventPublisher = eventBus;

        TimelineValidator timelineValidator = new TimelineValidator();

        TweetDao tweetDao = new TweetDao();
        TweetEditDao tweetEditDao = new TweetEditDao();

        UserDao userDao = new UserDao();

        TweetRepository tweetRepository = new JpaTweetRepository(
                tweetDao,
                tweetEditDao,
                em
        );

        UserRepository userRepository = new JpaUserRepository(userDao, em);

        TimelinePolicy timelinePolicy = new TimelinePolicy(userRepository);


        GetTimelineUseCase getTimelineUseCase = new GetTimelineUseCase(
                tweetRepository,
                timelineValidator,
                timelinePolicy,
                eventPublisher,
                timeProvider
        );

        return new TimelineFacade(getTimelineUseCase);
    }

    public static TweetFacade createTweetFacade(EntityManager em)
    {
        TimeProvider timeProvider = new TimeProvider();
        TokenGenerator tokenGenerator = new TokenGenerator();

        EventPublisher eventPublisher = eventBus;

        TweetValidator tweetValidator = new TweetValidator();
        TweetDao tweetDao = new TweetDao();
        TweetEditDao tweetEditDao = new TweetEditDao();

        UserDao userDao = new UserDao();

        FollowDao followDao = new FollowDao();
        BlockDao blockDao = new BlockDao();
        MuteDao muteDao = new MuteDao();
        LikeDao likeDao = new LikeDao();
        SessionDao sessionDao = new SessionDao(timeProvider);

        TweetRepository tweetRepository = new JpaTweetRepository(
                tweetDao,
                tweetEditDao,
                em
        );

        UserRepository userRepository = new JpaUserRepository(userDao, em);

        RelationshipRepository relationshipRepository = new JpaRelationshipRepository(
                followDao,
                blockDao,
                muteDao,
                likeDao,
                em
        );

        InteractionPolicy interactionPolicy = new InteractionPolicy(
                userRepository,
                relationshipRepository,
                tweetRepository
        );

        SessionRepository sessionRepository = new JpaSessionRepository(
                sessionDao,
                em
        );

        SessionFactory sessionFactory = new SessionFactory(tokenGenerator, timeProvider);

        SessionManager sessionManager = new SessionManager(
                sessionRepository,
                userRepository,
                sessionFactory,
                em
        );

        AuthLockOrchestrator lockOrchestrator = new AuthLockOrchestrator(
                userRepository,
                sessionManager
        );

        CreateTweetUseCase createTweetUseCase = new CreateTweetUseCase(
                tweetValidator,
                interactionPolicy,
                tweetRepository,
                userRepository,
                eventPublisher,
                timeProvider,
                lockOrchestrator
        );

        DeleteTweetUseCase deleteTweetUseCase = new DeleteTweetUseCase(
                tweetRepository,
                userRepository,
                eventPublisher,
                timeProvider,
                lockOrchestrator
        );

        EditTweetUseCase editTweetUseCase = new EditTweetUseCase(
                tweetValidator,
                interactionPolicy,
                tweetRepository,
                userRepository,
                eventPublisher,
                timeProvider,
                lockOrchestrator
        );

        LikeTweetUseCase likeTweetUseCase = new LikeTweetUseCase(
                interactionPolicy,
                tweetRepository,
                relationshipRepository,
                eventPublisher,
                timeProvider,
                lockOrchestrator
        );

        ReplyTweetUseCase replyTweetUseCase = new ReplyTweetUseCase(
                interactionPolicy,
                tweetRepository,
                userRepository,
                tweetValidator,
                eventPublisher,
                timeProvider,
                lockOrchestrator
        );

        RetweetUseCase retweetUseCase = new RetweetUseCase(
                interactionPolicy,
                tweetRepository,
                userRepository,
                eventPublisher,
                timeProvider,
                lockOrchestrator
        );

        UnlikeTweetUseCase unlikeTweetUseCase = new UnlikeTweetUseCase(
                interactionPolicy,
                tweetRepository,
                relationshipRepository,
                eventPublisher,
                timeProvider,
                lockOrchestrator
        );

        return new TweetFacade(
                createTweetUseCase,
                deleteTweetUseCase,
                editTweetUseCase,
                likeTweetUseCase,
                replyTweetUseCase,
                retweetUseCase,
                unlikeTweetUseCase
        );
    }

    public static UserFacade createUserFacade(EntityManager em)
    {

        TimeProvider timeProvider = new TimeProvider();
        TokenGenerator tokenGenerator = new TokenGenerator();

        EventPublisher eventPublisher = eventBus;

        TweetValidator tweetValidator = new TweetValidator();
        UserValidator userValidator = new UserValidator();
        TweetDao tweetDao = new TweetDao();
        TweetEditDao tweetEditDao = new TweetEditDao();

        UserDao userDao = new UserDao();

        FollowDao followDao = new FollowDao();
        BlockDao blockDao = new BlockDao();
        MuteDao muteDao = new MuteDao();
        LikeDao likeDao = new LikeDao();
        SessionDao sessionDao = new SessionDao(timeProvider);

        TweetRepository tweetRepository = new JpaTweetRepository(
                tweetDao,
                tweetEditDao,
                em
        );


        RelationshipRepository relationshipRepository = new JpaRelationshipRepository(
                followDao,
                blockDao,
                muteDao,
                likeDao,
                em
        );
        UserRepository userRepository = new JpaUserRepository(userDao, em);


        SessionRepository sessionRepository = new JpaSessionRepository(
                sessionDao,
                em
        );

        SessionFactory sessionFactory = new SessionFactory(tokenGenerator, timeProvider);

        SessionManager sessionManager = new SessionManager(
                sessionRepository,
                userRepository,
                sessionFactory,
                em
        );

        AuthLockOrchestrator lockOrchestrator = new AuthLockOrchestrator(
                userRepository,
                sessionManager
        );

        UserPolicy userPolicy = new UserPolicy(
                relationshipRepository,
                userRepository
        );

        MediaProperties mediaProperties = new MediaProperties("data/media");

        MediaStorageService mediaStorageService = new LocalMediaStorageService(
                mediaProperties
        );

        DeleteAccountUseCase deleteAccountUseCase = new DeleteAccountUseCase(
                userRepository,
                lockOrchestrator,
                timeProvider
        );

        GetProfileUseCase getProfileUseCase = new GetProfileUseCase(
               userRepository,
                lockOrchestrator,
                relationshipRepository,
                tweetRepository
       );


         SearchUsersUseCase searchUsersUseCase = new SearchUsersUseCase(
                 lockOrchestrator,
                 userRepository,
                 userPolicy,
                 userValidator
         );


         UpdateAvatarUseCase updateAvatarUseCase = new UpdateAvatarUseCase(
                 lockOrchestrator,
                 userRepository,
                 userValidator,
                 userPolicy,
                 mediaStorageService

         );


         UpdateBannerUseCase updateBannerUseCase = new UpdateBannerUseCase(
              lockOrchestrator,
                 userRepository,
                 userValidator,
                 userPolicy,
                 mediaStorageService
      );


         UpdateBioUseCase updateBioUseCase = new UpdateBioUseCase(
                 lockOrchestrator,
                 userRepository,
                 userValidator,
                 userPolicy
         );


         UpdateEmailUseCase updateEmailUseCase = new UpdateEmailUseCase(
                 userRepository,
                 lockOrchestrator
         );


       UpdatePasswordUseCase updatePasswordUseCase = new UpdatePasswordUseCase(
               userRepository,
               lockOrchestrator
       );


      UpdateProfileUseCase updateProfileUseCase = new UpdateProfileUseCase(
              userRepository,
              lockOrchestrator
      );


        UpdateCompleteProfileUseCase updateCompleteProfileUseCase = new UpdateCompleteProfileUseCase(
                lockOrchestrator,
                userRepository,
                relationshipRepository,
                tweetRepository,
                mediaStorageService,
                userValidator,
                userPolicy
        );


        return new UserFacade(
                deleteAccountUseCase,
                getProfileUseCase,
                searchUsersUseCase,
                updateAvatarUseCase,
                updateBannerUseCase,
                updateBioUseCase,
                updateEmailUseCase,
                updatePasswordUseCase,
                updateProfileUseCase,
                updateCompleteProfileUseCase
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
