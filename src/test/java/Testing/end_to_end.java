package Testing;

import Client.ClientApplicationContext;
import Client.Service.*;
import Client.config.ServerConfig;
import logic_core.app.dto.response.*;
import logic_core.common.result.Result;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class end_to_end extends XCloneTest2
{
    private ClientApplicationContext clientA;
    private ClientApplicationContext clientB;

    @BeforeEach
    void setupScenario()
    {
        super.beforeEach();

        ServerConfig config = new ServerConfig(HOST, serverPort, CLIENT_TIMEOUT);

        clientA = new ClientApplicationContext(config);
        clientB = new ClientApplicationContext(config);
    }

    @AfterEach
    void cleanupScenario()
    {
        if (clientA != null)
        {
            clientA.close();
        }

        if (clientB != null)
        {
            clientB.close();
        }
    }

    @Test
    void fullEndToEnd() {
        // ======================================================
// Phase 1
// Register + Login User A
// ======================================================

        AuthClientService authA = new AuthClientService(clientA);

        String usernameA = "user_a";
        String emailA = "user_a@test.com";
        String passwordA = "Password123!";
        String displayNameA = "User A";

// ------------------------------------------------------
// Register
// ------------------------------------------------------

        AuthClientService.AuthResult<AuthResponse> registerResult =
                authA.register(
                        usernameA,
                        emailA,
                        passwordA,
                        displayNameA
                ).join();

        assertTrue(registerResult.isSuccess(), registerResult.errorMessage());

        AuthResponse registerResponse = registerResult.data();

        assertNotNull(registerResponse);
        assertNotNull(registerResponse.userId());
        assertNotNull(registerResponse.token());

// بعد از Register باید Session ست شده باشد

        assertTrue(clientA.session().isLoggedIn());
        assertNotNull(clientA.session().getToken());
        assertNotNull(clientA.session().getCurrentUserId());

// ------------------------------------------------------
// Logout
// (می‌خواهیم Login را هم جداگانه تست کنیم)
// ------------------------------------------------------

        AuthClientService.AuthResult<LogoutResponse> logoutResult =
                authA.logout().join();

        assertTrue(logoutResult.isSuccess(), logoutResult.errorMessage());

        assertFalse(clientA.session().isLoggedIn());

// ------------------------------------------------------
// Login
// ------------------------------------------------------

        AuthClientService.AuthResult<AuthResponse> loginResult =
                authA.login(
                        usernameA,
                        passwordA
                ).join();

        assertTrue(loginResult.isSuccess(), loginResult.errorMessage());

        AuthResponse loginResponse = loginResult.data();

        assertNotNull(loginResponse);
        assertEquals(registerResponse.userId(), loginResponse.userId());

        assertTrue(clientA.session().isLoggedIn());
        assertNotNull(clientA.session().getToken());
        assertEquals(
                loginResponse.userId(),
                clientA.session().getCurrentUserId()
        );

// ------------------------------------------------------
// Database assertions
// ------------------------------------------------------

        assertEquals(1, db.countUsers());
        assertEquals(1, db.countSessions());
        assertTrue(db.userExists(usernameA));

        UUID userAId = loginResponse.userId();


        // ======================================================
// Phase 2
// Register + Login User B
// ======================================================

        AuthClientService authB = new AuthClientService(clientB);

        String usernameB = "user_b";
        String emailB = "user_b@test.com";
        String passwordB = "Password123!";
        String displayNameB = "User B";

// ------------------------------------------------------
// Register
// ------------------------------------------------------

        AuthClientService.AuthResult<AuthResponse> registerResultB =
                authB.register(
                        usernameB,
                        emailB,
                        passwordB,
                        displayNameB
                ).join();

        assertTrue(registerResultB.isSuccess(), registerResultB.errorMessage());

        AuthResponse registerResponseB = registerResultB.data();

        assertNotNull(registerResponseB);
        assertNotNull(registerResponseB.userId());
        assertNotNull(registerResponseB.token());

        assertTrue(clientB.session().isLoggedIn());
        assertNotNull(clientB.session().getToken());
        assertNotNull(clientB.session().getCurrentUserId());

// ------------------------------------------------------
// Logout
// ------------------------------------------------------

        AuthClientService.AuthResult<LogoutResponse> logoutResultB =
                authB.logout().join();

        assertTrue(logoutResultB.isSuccess(), logoutResultB.errorMessage());

        assertFalse(clientB.session().isLoggedIn());

// ------------------------------------------------------
// Login
// ------------------------------------------------------

        AuthClientService.AuthResult<AuthResponse> loginResultB =
                authB.login(
                        usernameB,
                        passwordB
                ).join();

        assertTrue(loginResultB.isSuccess(), loginResultB.errorMessage());

        AuthResponse loginResponseB = loginResultB.data();

        assertNotNull(loginResponseB);
        assertEquals(registerResponseB.userId(), loginResponseB.userId());

        assertTrue(clientB.session().isLoggedIn());
        assertNotNull(clientB.session().getToken());

        assertEquals(
                loginResponseB.userId(),
                clientB.session().getCurrentUserId()
        );

// ------------------------------------------------------
// Database assertions
// ------------------------------------------------------

        assertEquals(2, db.countUsers());
        assertEquals(2, db.countSessions());

        assertTrue(db.userExists(usernameA));
        assertTrue(db.userExists(usernameB));

        UUID userBId = loginResponseB.userId();

        // ======================================================
// Phase 3
// UserA follows UserB
// UserB follows UserA
// ======================================================

        RelationClientService relationA =
                new RelationClientService(clientA);

        RelationClientService relationB =
                new RelationClientService(clientB);

// ------------------------------------------------------
// A -> Follow B
// ------------------------------------------------------

        Result<FollowResponse> followAB =
                relationA.follow(userBId).join();

        assertTrue(followAB.isSuccess(), followAB.getError());

        FollowResponse followResponseAB =
                followAB.getData();

        assertNotNull(followResponseAB);

// ------------------------------------------------------
// B -> Follow A
// ------------------------------------------------------

        Result<FollowResponse> followBA =
                relationB.follow(userAId).join();

        assertTrue(followBA.isSuccess(), followBA.getError());

        FollowResponse followResponseBA =
                followBA.getData();

        assertNotNull(followResponseBA);

// ------------------------------------------------------
// Database Assertions
// ------------------------------------------------------

        assertEquals(
                2,
                db.countFollows()
        );

        assertTrue(db.isFollowing(userAId, userBId));
        assertTrue(db.isFollowing(userBId, userAId));


        // ======================================================
// Phase 4
// Conversation + First Message
// ======================================================

        ConversationClientService conversationA =
                new ConversationClientService(clientA);

        MessageClientService messageA =
                new MessageClientService(clientA);

        MessageClientService messageB =
                new MessageClientService(clientB);

// ------------------------------------------------------
// A creates conversation with B
// ------------------------------------------------------

        Result<CreateConversationResponse> createConversation =
                conversationA.createConversation(
                        userAId,
                        java.util.List.of(userBId)
                ).join();

        assertTrue(
                createConversation.isSuccess(),
                createConversation.getError()
        );

        CreateConversationResponse createConversationResponse =
                createConversation.getData();

        assertNotNull(createConversationResponse);

        UUID conversationId =
                createConversationResponse.conversationId();

        assertNotNull(conversationId);

// ------------------------------------------------------
// Database
// ------------------------------------------------------

        assertEquals(
                1,
                db.countConversations()
        );

// ------------------------------------------------------
// A sends first message
// ------------------------------------------------------

        String firstMessage =
                "Hello from User A";

        Result<ConversationStateResponse> sendMessage =
                messageA.sendMessage(
                        conversationId,
                        firstMessage
                ).join();

        assertTrue(
                sendMessage.isSuccess(),
                sendMessage.getError()
        );

        ConversationStateResponse stateResponse =
                sendMessage.getData();

        assertNotNull(stateResponse);

// ------------------------------------------------------
// Database
// ------------------------------------------------------

        assertEquals(
                1,
                db.countMessages()
        );

// ------------------------------------------------------
// B loads conversation messages
// ------------------------------------------------------

        Result<ConversationMessagesResponse> messages =
                messageB.getConversationMessages(
                        conversationId,
                        1,
                        20
                ).join();

        assertTrue(
                messages.isSuccess(),
                messages.getError()
        );

        ConversationMessagesResponse conversationMessages =
                messages.getData();

        assertNotNull(conversationMessages);

        assertEquals(
                1,
                conversationMessages.messages().size()
        );

        MessageInfoResponse first =
                conversationMessages.messages().getFirst();

        assertEquals(
                firstMessage,
                first.content()
        );

        assertEquals(
                userAId,
                first.senderId()
        );

// ======================================================
// Phase 5
// Tweet + Like
// ======================================================

        TweetClientService tweetA =
                new TweetClientService(clientA);

        TweetClientService tweetB =
                new TweetClientService(clientB);

// ------------------------------------------------------
// A creates tweet
// ------------------------------------------------------

        String tweetContent =
                "Hello X-Clone 🚀";

        Result<TweetResponse> createTweet =
                tweetA.createTweet(
                        tweetContent,
                        null,
                        null,
                        List.of(),
                        null
                ).join();

        assertTrue(
                createTweet.isSuccess(),
                createTweet.getError()
        );

        TweetResponse tweetResponse =
                createTweet.getData();

        assertNotNull(tweetResponse);

// اگر اسم متد فرق داشت فقط این خط را اصلاح کن
        UUID tweetId =
                tweetResponse.id();

        assertNotNull(tweetId);

// ------------------------------------------------------
// Database
// ------------------------------------------------------

        assertEquals(
                1,
                db.countTweets()
        );

// ------------------------------------------------------
// B likes tweet
// ------------------------------------------------------

        Result<LikeResponse> likeResult =
                tweetB.likeTweet(tweetId).join();

        assertTrue(
                likeResult.isSuccess(),
                likeResult.getError()
        );

        LikeResponse likeResponse =
                likeResult.getData();

        assertNotNull(likeResponse);

// ------------------------------------------------------
// Database
// ------------------------------------------------------

        assertEquals(
                1,
                db.countLikes()
        );




        // ======================================================
// Phase 6
// Reply + Unlike + Edit + Delete Tweet
// ======================================================

// ------------------------------------------------------
// B replies to A's Tweet
// ------------------------------------------------------

        String replyContent = "Nice tweet!";

        Result<TweetResponse> replyResult =
                tweetB.replyTweet(
                        tweetId,
                        replyContent,
                        List.of()
                ).join();

        assertTrue(
                replyResult.isSuccess(),
                replyResult.getError()
        );

        TweetResponse replyResponse =
                replyResult.getData();

        assertNotNull(replyResponse);

// اگر TweetResponse متد دیگری دارد فقط این خط را اصلاح کن
        UUID replyTweetId =
                replyResponse.id();

        assertNotNull(replyTweetId);

// ------------------------------------------------------
// Database
// ------------------------------------------------------

        assertEquals(
                2,
                db.countTweets()
        );


// ------------------------------------------------------
// B unlikes A's Tweet
// ------------------------------------------------------

        Result<LikeResponse> unlikeResult =
                tweetB.unlikeTweet(tweetId)
                        .join();

        assertTrue(
                unlikeResult.isSuccess(),
                unlikeResult.getError()
        );

        assertNotNull(
                unlikeResult.getData()
        );

// ------------------------------------------------------
// Database
// ------------------------------------------------------

        assertEquals(
                0,
                db.countLikes()
        );


// ------------------------------------------------------
// A edits original Tweet
// ------------------------------------------------------

        String editedContent =
                "Hello X-Clone (edited)";

        Result<TweetResponse> editResult =
                tweetA.editTweet(
                        tweetId,
                        editedContent
                ).join();

        assertTrue(
                editResult.isSuccess(),
                editResult.getError()
        );

        TweetResponse editedTweet =
                editResult.getData();

        assertNotNull(editedTweet);

// ------------------------------------------------------
// A deletes reply Tweet
// ------------------------------------------------------

        Result<TweetResponse> deleteReply =
                tweetB.deleteTweet(replyTweetId)
                        .join();

        assertTrue(
                deleteReply.isSuccess(),
                deleteReply.getError()
        );

// ------------------------------------------------------
// Database
// ------------------------------------------------------

        assertEquals(
                1,
                db.countTweets()
        );

// ------------------------------------------------------
// A deletes original Tweet
// ------------------------------------------------------

        Result<TweetResponse> deleteOriginal =
                tweetA.deleteTweet(tweetId)
                        .join();

        assertTrue(
                deleteOriginal.isSuccess(),
                deleteOriginal.getError()
        );

// ------------------------------------------------------
// Database
// ------------------------------------------------------

        assertEquals(
                0,
                db.countTweets()
        );



        // ======================================================
// Phase 7
// Profile + Search + Delete Conversation + Logout
// ======================================================

        UserClientService userA =
                new UserClientService(clientA);

        UserClientService userB =
                new UserClientService(clientB);

        ConversationClientService conversationB =
                new ConversationClientService(clientB);

// ------------------------------------------------------
// UserA loads own profile
// ------------------------------------------------------

        Result<ProfileInfoResponse> profileResult =
                userA.getProfile(userAId).join();

        assertTrue(
                profileResult.isSuccess(),
                profileResult.getError()
        );

        ProfileInfoResponse profile =
                profileResult.getData();

        assertNotNull(profile);

        assertEquals(
                userAId,
                profile.userId()
        );

        assertEquals(
                usernameA,
                profile.username()
        );

// ------------------------------------------------------
// UserB searches UserA
// ------------------------------------------------------

        Result<List<UserSearchResponse>> searchResult =
                userB.searchUsers(
                        "user_",
                        20,
                        0
                ).join();

        assertTrue(
                searchResult.isSuccess(),
                searchResult.getError()
        );

        assertFalse(
                searchResult.getData().isEmpty()
        );

// ------------------------------------------------------
// UserB gets conversation list
// ------------------------------------------------------

        Result<GetConversationsResponse> conversations =
                conversationB.getConversations(
                        1,
                        20
                ).join();

        assertTrue(
                conversations.isSuccess(),
                conversations.getError()
        );

        assertFalse(
                conversations.getData()
                        .conversations()
                        .isEmpty()
        );

// ------------------------------------------------------
// Delete Conversation
// ------------------------------------------------------

        UUID conversationId2 =
                conversations.getData()
                        .conversations()
                        .getFirst().conversationId();

        Result<DeleteConversationResponse> deleteConversation =
                conversationB.deleteConversation(
                        conversationId2,
                        userBId
                ).join();

        assertTrue(
                deleteConversation.isSuccess(),
                deleteConversation.getError()
        );

        assertEquals(
                0,
                db.countConversations()
        );

        assertEquals(
                0,
                db.countMessages()
        );

// ------------------------------------------------------
// Logout UserA
// ------------------------------------------------------

        AuthClientService.AuthResult<LogoutResponse> logoutA =
                authA.logout().join();

        assertTrue(
                logoutA.isSuccess(),
                logoutA.errorMessage()
        );

        assertFalse(
                clientA.session().isLoggedIn()
        );

// ------------------------------------------------------
// Logout UserB
// ------------------------------------------------------

        AuthClientService.AuthResult<LogoutResponse> logoutB =
                authB.logout().join();

        assertTrue(
                logoutB.isSuccess(),
                logoutB.errorMessage()
        );

        assertFalse(
                clientB.session().isLoggedIn()
        );

// ------------------------------------------------------
// Final Database Assertions
// ------------------------------------------------------

        assertEquals(
                0,
                db.countSessions()
        );


        assertEquals(0, db.countSessions());
        assertEquals(0, db.countTweets());
        assertEquals(0, db.countLikes());
        assertEquals(0, db.countMessages());
        assertEquals(0, db.countConversations());
    }


}
