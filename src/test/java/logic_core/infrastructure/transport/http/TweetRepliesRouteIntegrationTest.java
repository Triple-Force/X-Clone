package logic_core.infrastructure.transport.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import logic_core.app.dto.request.CreateTweetRequest;
import logic_core.app.dto.request.GetRepliesRequest;
import logic_core.app.dto.request.RegisterRequest;
import logic_core.app.dto.request.ReplyTweetRequest;
import logic_core.app.dto.response.AuthResponse;
import logic_core.app.dto.response.TweetResponse;
import logic_core.app.dto.timeline.TimelineTweet;
import logic_core.infrastructure.transport.RequestEnvelope;
import logic_core.infrastructure.transport.RequestType;
import logic_core.infrastructure.transport.ResponseEnvelope;
import logic_core.infrastructure.transport.server.ServerMain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for the TWEET_GET_REPLIES transport route (previously a
 * stub that returned {@code null}).
 *
 * <p>Drives the full stack through {@code POST /api}:
 *
 * <pre>
 * register A, B
 *   → A creates parent tweet
 *   → B replies twice
 *   → TWEET_GET_REPLIES → success envelope whose payload is a JSON array of
 *     {@link TimelineTweet} that the existing client DTO can deserialize
 * </pre>
 *
 * Also verifies unauthenticated requests are rejected with a failure envelope
 * instead of a raw {@code null} body.
 */
@SpringBootTest(classes = ServerMain.class)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TweetRepliesRouteIntegrationTest {

    private static final int TEST_SOCKET_PORT = findFreePort();

    @DynamicPropertySource
    static void registerTestProperties(DynamicPropertyRegistry registry) {
        registry.add("server.socket.port", () -> TEST_SOCKET_PORT);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final List<UUID> createdTweetIds = new ArrayList<>();
    private final List<UUID> createdUserIds = new ArrayList<>();

    @AfterEach
    void cleanUpCreatedRows() {
        try {
            if (!createdTweetIds.isEmpty()) {
                String placeholders = repeatPlaceholders(createdTweetIds.size());
                Object[] tweetArgs = createdTweetIds.toArray();
                jdbcTemplate.update(
                        "DELETE FROM likes WHERE tweet_id IN (" + placeholders + ")", tweetArgs);
                jdbcTemplate.update(
                        "DELETE FROM tweet_edits WHERE tweet_id IN (" + placeholders + ")", tweetArgs);
                jdbcTemplate.update(
                        "DELETE FROM tweets WHERE id IN (" + placeholders + ")", tweetArgs);
            }
            if (!createdUserIds.isEmpty()) {
                String placeholders = repeatPlaceholders(createdUserIds.size());
                Object[] userArgs = createdUserIds.toArray();
                jdbcTemplate.update(
                        "DELETE FROM sessions WHERE user_id IN (" + placeholders + ")", userArgs);
                jdbcTemplate.update(
                        "DELETE FROM users WHERE id IN (" + placeholders + ")", userArgs);
            }
        } catch (Exception e) {
            System.err.println("TweetRepliesRouteIntegrationTest cleanup warning: " + e.getMessage());
        }
    }

    @Test
    void replies_authenticatedParentAuthor_seesRepliesOldestFirst() throws Exception {
        AuthResponse userA = registerUser("repa");
        AuthResponse userB = registerUser("repb");

        TweetResponse parent = createTweet(userA, "reply-parent-1");

        TweetResponse first = reply(userB, parent.id(), "reply-first");
        TweetResponse second = reply(userB, parent.id(), "reply-second");

        ResponseEnvelope envelope = sendReplies(parent.id(), userA.token());
        assertSuccess(envelope, "parent author fetches replies");

        List<TimelineTweet> replies = gson.fromJson(
                envelope.getData(),
                TypeToken.getParameterized(List.class, TimelineTweet.class).getType());

        assertThat(replies)
                .as("reply thread must contain both replies in oldest-first order")
                .extracting(TimelineTweet::content)
                .containsExactly("reply-first", "reply-second");
        assertThat(replies)
                .allSatisfy(r -> {
                    assertThat(r.authorId()).isEqualTo(userB.userId());
                    assertThat(r.tweetId()).isNotNull();
                    assertThat(r.username()).isNotBlank();
                    assertThat(r.publishedAt()).isNotNull();
                });
        assertThat(replies).extracting(TimelineTweet::tweetId)
                .contains(first.id(), second.id());
    }

    @Test
    void replies_authenticatedOtherUser_canReadPublicThread() throws Exception {
        AuthResponse userA = registerUser("repc");
        AuthResponse userB = registerUser("repd");
        AuthResponse userC = registerUser("repe");

        TweetResponse parent = createTweet(userA, "reply-parent-2");
        reply(userB, parent.id(), "reply-third");

        // C is not involved in the thread but holds a valid session: public
        // tweet threads stay readable (same posture as timelines).
        ResponseEnvelope envelope = sendReplies(parent.id(), userC.token());
        assertSuccess(envelope, "unrelated authenticated user reads public thread");

        List<TimelineTweet> replies = gson.fromJson(
                envelope.getData(),
                TypeToken.getParameterized(List.class, TimelineTweet.class).getType());
        assertThat(replies).extracting(TimelineTweet::content).containsExactly("reply-third");
    }

    @Test
    void replies_unauthenticated_rejectedWithUnauthorized() throws Exception {
        AuthResponse userA = registerUser("repf");
        TweetResponse parent = createTweet(userA, "reply-parent-3");

        // Missing credentials on a protected route are now rejected by the HTTP
        // authentication layer with 401 Unauthorized and a failure envelope.
        ResponseEnvelope envelope = sendUnauthorized(
                new RequestEnvelope(
                        UUID.randomUUID(),
                        RequestType.TWEET_GET_REPLIES,
                        gson.toJsonTree(new GetRepliesRequest(parent.id(), null)),
                        null));
        assertThat(envelope)
                .as("route must return a failure envelope, not null")
                .isNotNull();
        assertThat(envelope.isSuccess()).isFalse();
        assertThat(envelope.errorCode()).isEqualTo("AUTH_REQUIRED");
    }

    @Test
    void replies_noReplies_returnsEmptyArray() throws Exception {
        AuthResponse userA = registerUser("repg");
        TweetResponse parent = createTweet(userA, "reply-parent-4");

        ResponseEnvelope envelope = sendReplies(parent.id(), userA.token());
        assertSuccess(envelope, "empty thread fetch");

        List<TimelineTweet> replies = gson.fromJson(
                envelope.getData(),
                TypeToken.getParameterized(List.class, TimelineTweet.class).getType());
        assertThat(replies).isEmpty();
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private AuthResponse registerUser(String prefix) throws Exception {
        String username = prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest registerRequest = new RegisterRequest(
                username,
                username + "@repliestest.com",
                "StrongPassword123!",
                "Display " + prefix);

        ResponseEnvelope envelope = send(new RequestEnvelope(
                UUID.randomUUID(),
                RequestType.AUTH_REGISTER,
                gson.toJsonTree(registerRequest),
                null));
        assertSuccess(envelope, "register " + username);

        AuthResponse auth = gson.fromJson(envelope.getData(), AuthResponse.class);
        createdUserIds.add(auth.userId());
        return auth;
    }

    private TweetResponse createTweet(AuthResponse auth, String content) throws Exception {
        CreateTweetRequest createRequest = new CreateTweetRequest(content, null, null, null, auth.token(), null);
        ResponseEnvelope envelope = send(new RequestEnvelope(
                UUID.randomUUID(),
                RequestType.TWEET_CREATE,
                gson.toJsonTree(createRequest),
                null));
        assertSuccess(envelope, "create tweet " + content);

        TweetResponse tweet = gson.fromJson(envelope.getData(), TweetResponse.class);
        createdTweetIds.add(tweet.id());
        return tweet;
    }

    private TweetResponse reply(AuthResponse author, UUID parentId, String text) throws Exception {
        ReplyTweetRequest replyRequest =
                new ReplyTweetRequest(parentId, text, null, author.token());
        ResponseEnvelope envelope = send(new RequestEnvelope(
                UUID.randomUUID(),
                RequestType.TWEET_REPLY,
                gson.toJsonTree(replyRequest),
                null));
        assertSuccess(envelope, "reply " + text);

        TweetResponse tweet = gson.fromJson(envelope.getData(), TweetResponse.class);
        createdTweetIds.add(tweet.id());
        return tweet;
    }

    private ResponseEnvelope sendReplies(UUID tweetId, String token) throws Exception {
        GetRepliesRequest repliesRequest = new GetRepliesRequest(tweetId, token);
        return send(new RequestEnvelope(
                UUID.randomUUID(),
                RequestType.TWEET_GET_REPLIES,
                gson.toJsonTree(repliesRequest),
                null));
    }

    private ResponseEnvelope send(RequestEnvelope request) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        ResponseEnvelope envelope = gson.fromJson(response.getContentAsString(), ResponseEnvelope.class);
        assertThat(envelope).isNotNull();
        return envelope;
    }

    private ResponseEnvelope sendUnauthorized(RequestEnvelope request) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(request)))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse();

        ResponseEnvelope envelope = gson.fromJson(response.getContentAsString(), ResponseEnvelope.class);
        assertThat(envelope).isNotNull();
        return envelope;
    }

    private void assertSuccess(ResponseEnvelope envelope, String step) {
        assertThat(envelope.isSuccess())
                .as("%s — errorCode=%s, errorMessage=%s", step, envelope.errorCode(), envelope.errorMessage())
                .isTrue();
    }

    private static String repeatPlaceholders(int count) {
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                placeholders.append(",");
            }
            placeholders.append("?");
        }
        return placeholders.toString();
    }

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (Exception e) {
            throw new IllegalStateException("Could not find a free TCP port", e);
        }
    }
}
