package Testing;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import logic_core.app.dto.request.*;
import logic_core.app.dto.response.*;
import logic_core.infrastructure.transport.RequestEnvelope;
import logic_core.infrastructure.transport.RequestType;
import logic_core.infrastructure.transport.ResponseEnvelope;
import logic_core.infrastructure.transport.server.ServerMain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end socket-level test of the whole messaging flow against the fully
 * migrated Spring stack:
 *
 * <pre>
 * register (A, B, C)
 *   → create conversation (A + B)
 *   → add member (C)
 *   → send / read / edit / delete messages (A)
 *   → delete conversation (A)
 * </pre>
 *
 * The test boots the real Spring application ({@link ServerMain}) so the real
 * Spring-managed {@code SocketServer}, {@code RequestDispatcher}, Facades,
 * UseCases and the migrated Spring Data repositories handle every request.
 * Conversations/messages/users are created through the TCP protocol exactly like
 * a real client.
 */
@SpringBootTest(classes = ServerMain.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MessagingFlowSocketIntegrationTest {

    private static final int CLIENT_SOCKET_TIMEOUT_MS = 8000;
    private static final int SERVER_STARTUP_TIMEOUT_MS = 15_000;

    private static final int TEST_SERVER_PORT = findFreePort();

    @DynamicPropertySource
    static void registerTestProperties(DynamicPropertyRegistry registry) {
        registry.add("server.socket.port", () -> TEST_SERVER_PORT);
    }

    @Autowired
    private Gson gson;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final List<UUID> createdUserIds = new ArrayList<>();
    private final List<UUID> createdConversationIds = new ArrayList<>();

    @BeforeAll
    void beforeAll() throws Exception {
        // SocketServer is started by Spring via @PostConstruct; just wait until it accepts.
        waitForServerToBecomeAvailable();
    }

    @AfterEach
    void cleanUpCreatedRows() {
        // Best-effort cleanup of rows created by this test (random usernames make
        // leftovers harmless, but keeping the dev DB tidy is nicer). Runs in the
        // correct dependency order: conversations first (messages/members cascade),
        // then sessions and users.
        try {
            for (UUID conversationId : createdConversationIds) {
                jdbcTemplate.update("DELETE FROM direct_messages WHERE conversation_id = ?", conversationId);
                jdbcTemplate.update("DELETE FROM conversation_members WHERE conversation_id = ?", conversationId);
                jdbcTemplate.update("DELETE FROM conversations WHERE id = ?", conversationId);
            }
            if (!createdUserIds.isEmpty()) {
                StringBuilder placeholders = new StringBuilder();
                for (int i = 0; i < createdUserIds.size(); i++) {
                    if (i > 0) {
                        placeholders.append(",");
                    }
                    placeholders.append("?");
                }
                jdbcTemplate.update(
                        "DELETE FROM sessions WHERE user_id IN (" + placeholders + ")",
                        createdUserIds.toArray()
                );
                jdbcTemplate.update(
                        "DELETE FROM users WHERE id IN (" + placeholders + ")",
                        createdUserIds.toArray()
                );
            }
        } catch (Exception e) {
            // Cleanup is best-effort only; never mask the actual test result.
            System.err.println("MessagingFlowSocketIntegrationTest cleanup warning: " + e.getMessage());
        }
    }

    // ========================================================================
    // The full messaging flow
    // ========================================================================

    @Test
    void fullMessagingFlow_createConversation_addMember_sendEditDeleteMessages_deleteConversation()
            throws Exception {
        // ---------------------------------------------------------------
        // 1. Register three users over the socket protocol
        // ---------------------------------------------------------------
        AuthResponse userA = registerUser("msgA");
        AuthResponse userB = registerUser("msgB");
        AuthResponse userC = registerUser("msgC");

        assertThat(userA.token()).isNotBlank();
        assertThat(userB.token()).isNotBlank();
        assertThat(userC.token()).isNotBlank();

        // ---------------------------------------------------------------
        // 2. User A creates a 1:1 conversation with B
        // ---------------------------------------------------------------
        CreateConversationRequest createRequest =
                new CreateConversationRequest(userA.userId(), List.of(userB.userId()), userA.token());

        ResponseEnvelope createResponse = send(RequestType.CONVERSATION_CREATE, createRequest);
        assertSuccess(createResponse, "create conversation");

        UUID conversationId = gson.fromJson(createResponse.getData(), CreateConversationResponse.class)
                .conversationId();
        assertThat(conversationId).isNotNull();
        createdConversationIds.add(conversationId);

        // ---------------------------------------------------------------
        // 3. User A adds C to the conversation
        // ---------------------------------------------------------------
        AddConversationMemberRequest addRequest =
                new AddConversationMemberRequest(conversationId, userC.userId(), userA.token());

        assertSuccess(send(RequestType.MEMBER_ADD, addRequest), "add member");

        // C is now a participant and can read the conversation
        GetConversationMessagesRequest asMemberRequest =
                new GetConversationMessagesRequest(conversationId, 1, 10, userC.token());
        ResponseEnvelope memberRead = send(RequestType.MESSAGE_GET_CONVERSATION, asMemberRequest);
        assertSuccess(memberRead, "member C reads empty conversation");
        assertThat(gson.fromJson(memberRead.getData(), ConversationMessagesResponse.class).messages()).isEmpty();

        // ---------------------------------------------------------------
        // 4. User A sends messages
        // ---------------------------------------------------------------
        SendMessageRequest firstSend =
                new SendMessageRequest(conversationId, "Hello B", userA.token());
        ResponseEnvelope sendOne = send(RequestType.MESSAGE_SEND, firstSend);
        assertSuccess(sendOne, "send first message");
        ConversationStateResponse sendOneState =
                gson.fromJson(sendOne.getData(), ConversationStateResponse.class);
        assertThat(sendOneState.conversationId()).isEqualTo(conversationId);
        assertThat(sendOneState.lastMessagePreview()).isEqualTo("Hello B");

        SendMessageRequest secondSend =
                new SendMessageRequest(conversationId, "Second message", userA.token());
        assertSuccess(send(RequestType.MESSAGE_SEND, secondSend), "send second message");

        // ---------------------------------------------------------------
        // 5. Read the messages back (ordered oldest first)
        // ---------------------------------------------------------------
        GetConversationMessagesRequest readRequest =
                new GetConversationMessagesRequest(conversationId, 1, 10, userA.token());
        ResponseEnvelope readResponse = send(RequestType.MESSAGE_GET_CONVERSATION, readRequest);
        assertSuccess(readResponse, "read conversation messages");

        ConversationMessagesResponse messages =
                gson.fromJson(readResponse.getData(), ConversationMessagesResponse.class);
        assertThat(messages.conversationId()).isEqualTo(conversationId);
        assertThat(messages.messages()).hasSize(2);

        MessageInfoResponse firstMessage = messages.messages().get(0);
        MessageInfoResponse secondMessage = messages.messages().get(1);

        assertThat(firstMessage.content()).isEqualTo("Hello B");
        assertThat(secondMessage.content()).isEqualTo("Second message");
        assertThat(firstMessage.senderId()).isEqualTo(userA.userId());
        assertThat(firstMessage.conversationId()).isEqualTo(conversationId);
        assertThat(firstMessage.isRead()).isFalse();
        assertThat(firstMessage.edited()).isFalse();
        assertThat(firstMessage.createdAt()).isNotNull();

        // ---------------------------------------------------------------
        // 6. User B (the other participant) can read the conversation too
        // ---------------------------------------------------------------
        GetConversationMessagesRequest receiverRead =
                new GetConversationMessagesRequest(conversationId, 1, 10, userB.token());
        ResponseEnvelope receiverResponse = send(RequestType.MESSAGE_GET_CONVERSATION, receiverRead);
        assertSuccess(receiverResponse, "receiver reads conversation");
        assertThat(gson.fromJson(receiverResponse.getData(), ConversationMessagesResponse.class).messages())
                .hasSize(2);

        // ---------------------------------------------------------------
        // 7. User A edits the first message
        // ---------------------------------------------------------------
        EditMessageRequest editRequest =
                new EditMessageRequest(conversationId, firstMessage.messageId(), "Hello B (edited)", userA.token());
        ResponseEnvelope editResponse = send(RequestType.MESSAGE_EDIT, editRequest);
        assertSuccess(editResponse, "edit message");

        // The edited content is visible when reading the message
        GetMessageRequest getEdited =
                new GetMessageRequest(conversationId, firstMessage.messageId(), userA.token());
        ResponseEnvelope getEditedResponse = send(RequestType.MESSAGE_GET, getEdited);
        assertSuccess(getEditedResponse, "get edited message");
        assertThat(gson.fromJson(getEditedResponse.getData(), MessageInfoResponse.class).content())
                .isEqualTo("Hello B (edited)");

        // ...and in the conversation listing
        ResponseEnvelope afterEditList = send(RequestType.MESSAGE_GET_CONVERSATION, readRequest);
        assertSuccess(afterEditList, "list after edit");
        assertThat(gson.fromJson(afterEditList.getData(), ConversationMessagesResponse.class).messages())
                .extracting(MessageInfoResponse::content)
                .containsExactly("Hello B (edited)", "Second message");

        // ---------------------------------------------------------------
        // 8. User A deletes the edited message
        // ---------------------------------------------------------------
        DeleteMessageRequest deleteMessageRequest =
                new DeleteMessageRequest(conversationId, firstMessage.messageId(), userA.token());
        ResponseEnvelope deleteMessageResponse = send(RequestType.MESSAGE_DELETE, deleteMessageRequest);
        assertSuccess(deleteMessageResponse, "delete message");
        assertThat(gson.fromJson(deleteMessageResponse.getData(), ConversationStateResponse.class)
                .lastMessagePreview()).isEqualTo("Second message");

        ResponseEnvelope afterDeleteList = send(RequestType.MESSAGE_GET_CONVERSATION, readRequest);
        assertSuccess(afterDeleteList, "list after message delete");
        assertThat(gson.fromJson(afterDeleteList.getData(), ConversationMessagesResponse.class).messages())
                .extracting(MessageInfoResponse::content)
                .containsExactly("Second message");

        // ---------------------------------------------------------------
        // 9. The conversation appears in User A's conversation list
        // ---------------------------------------------------------------
        GetConversationsRequest getConversationsRequest =
                new GetConversationsRequest(1, 10, userA.token());
        ResponseEnvelope conversationsResponse = send(RequestType.CONVERSATION_GET, getConversationsRequest);
        assertSuccess(conversationsResponse, "get conversations");

        JsonArray conversations = conversationsResponse.getData()
                .getAsJsonObject()
                .getAsJsonArray("conversations");
        assertThat(conversations).isNotEmpty();
        boolean found = false;
        for (JsonElement element : conversations) {
            ConversationSummaryResponse summary =
                    gson.fromJson(element, ConversationSummaryResponse.class);
            if (conversationId.equals(summary.conversationId())) {
                found = true;
                assertThat(summary.lastMessage()).isEqualTo("Second message");
            }
        }
        assertThat(found).as("conversation must appear in the owner list").isTrue();

        // ---------------------------------------------------------------
        // 10. User A deletes the conversation
        // ---------------------------------------------------------------
        DeleteConversationRequest deleteConversationRequest =
                new DeleteConversationRequest(conversationId, userA.userId(), userA.token());
        ResponseEnvelope deleteConversationResponse = send(RequestType.CONVERSATION_DELETE, deleteConversationRequest);
        assertSuccess(deleteConversationResponse, "delete conversation");

        // The conversation disappears from A's list
        ResponseEnvelope afterDeleteListResponse = send(RequestType.CONVERSATION_GET, getConversationsRequest);
        assertSuccess(afterDeleteListResponse, "get conversations after delete");
        JsonArray remaining = afterDeleteListResponse.getData()
                .getAsJsonObject()
                .getAsJsonArray("conversations");
        boolean stillThere = false;
        for (JsonElement element : remaining) {
            if (conversationId.equals(
                    gson.fromJson(element, ConversationSummaryResponse.class).conversationId())) {
                stillThere = true;
            }
        }
        assertThat(stillThere).as("deleted conversation must be gone from the list").isFalse();

        // Messaging against the deleted conversation now fails for every participant
        ResponseEnvelope afterConversationDeleteRead =
                send(RequestType.MESSAGE_GET_CONVERSATION, readRequest);
        assertThat(afterConversationDeleteRead.isSuccess())
                .as("reading messages of a deleted conversation must fail")
                .isFalse();
    }

    // ========================================================================
    // Socket helpers
    // ========================================================================

    private AuthResponse registerUser(String prefix) throws Exception {
        String username = prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
        String email = username + "@integrationtest.com";
        String password = "StrongPassword123!";

        RegisterRequest registerRequest =
                new RegisterRequest(username, email, password, "Display " + prefix);

        RequestEnvelope envelope = new RequestEnvelope(
                UUID.randomUUID(),
                RequestType.AUTH_REGISTER,
                gson.toJsonTree(registerRequest),
                null
        );

        ResponseEnvelope response = send(envelope);
        assertSuccess(response, "register user " + username);

        AuthResponse auth = gson.fromJson(response.getData(), AuthResponse.class);
        assertThat(auth.userId()).isNotNull();
        assertThat(auth.token()).isNotBlank();
        createdUserIds.add(auth.userId());
        return auth;
    }

    private ResponseEnvelope send(RequestType type, Object payload) throws Exception {
        return send(new RequestEnvelope(
                UUID.randomUUID(),
                type,
                gson.toJsonTree(payload),
                null
        ));
    }

    private ResponseEnvelope send(RequestEnvelope request) throws Exception {
        try (Socket socket = openClientSocket();
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()))) {
            out.println(gson.toJson(request));

            String raw = in.readLine();
            assertThat(raw)
                    .as("server must return a response for request " + request.type())
                    .isNotNull();

            ResponseEnvelope response = gson.fromJson(raw, ResponseEnvelope.class);
            assertThat(response).isNotNull();
            return response;
        }
    }

    private Socket openClientSocket() throws Exception {
        Socket socket = new Socket("127.0.0.1", TEST_SERVER_PORT);
        socket.setSoTimeout(CLIENT_SOCKET_TIMEOUT_MS);
        return socket;
    }

    private void assertSuccess(ResponseEnvelope response, String step) {
        assertThat(response.isSuccess())
                .as("%s — errorCode=%s, errorMessage=%s",
                        step, response.errorCode(), response.errorMessage())
                .isTrue();
    }

    private void waitForServerToBecomeAvailable() throws Exception {
        long deadline = System.currentTimeMillis() + SERVER_STARTUP_TIMEOUT_MS;

        while (System.currentTimeMillis() < deadline) {
            try (Socket ignored = new Socket("127.0.0.1", TEST_SERVER_PORT)) {
                return;
            } catch (Exception ignored) {
                Thread.sleep(100);
            }
        }

        throw new IllegalStateException("SocketServer did not become available on port " + TEST_SERVER_PORT);
    }

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (Exception e) {
            throw new IllegalStateException("Could not find a free TCP port", e);
        }
    }
}
