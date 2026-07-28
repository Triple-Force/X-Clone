package Testing;

import Client.Service.AuthClientService;
import Client.Service.ConversationClientService;
import Client.Service.MessageClientService;
import logic_core.app.dto.response.*;
import logic_core.common.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class MessageTest extends XCloneTest2
{
    private static final long TIMEOUT_SECONDS = 10;

    @Test
    @DisplayName("should complete message lifecycle")
    void shouldCompleteMessageLifecycle() throws Exception
    {
        //--------------------------------------------------
        // Services
        //--------------------------------------------------

        AuthClientService auth = new AuthClientService(client);

        ConversationClientService conversations = new ConversationClientService(client);

        MessageClientService messages = new MessageClientService(client);

        //--------------------------------------------------
        // Register User A
        //--------------------------------------------------

        String usernameA = "userA_" + UUID.randomUUID().toString().substring(0, 8);

        String password = "Password123!";

        AuthClientService.AuthResult<AuthResponse> userA =
                auth.register(
                                usernameA,
                                usernameA + "@example.com",
                                password,
                                "User A"
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(userA.isSuccess());

        //--------------------------------------------------
        // Register User B
        //--------------------------------------------------

        rebuildClient();

        auth = new AuthClientService(client);

        String usernameB = "userB_" + UUID.randomUUID().toString().substring(0, 8);

        AuthClientService.AuthResult<AuthResponse> userB =
                auth.register(
                                usernameB,
                                usernameB + "@example.com",
                                password,
                                "User B"
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(userB.isSuccess());

        //--------------------------------------------------
        // Login User A
        //--------------------------------------------------

        rebuildClient();

        auth = new AuthClientService(client);
        conversations = new ConversationClientService(client);
        messages = new MessageClientService(client);

        assertTrue(
                auth.login(
                                usernameA,
                                password
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .isSuccess()
        );

        //--------------------------------------------------
        // Create Conversation
        //--------------------------------------------------

        Result<CreateConversationResponse> conversation =
                conversations.createConversation(
                                userA.data().userId(),
                                List.of(userB.data().userId())
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertNotNull(conversation);

        UUID conversationId = conversation.getData().conversationId();

        //--------------------------------------------------
        // Send Message
        //--------------------------------------------------

        String original = "Hello World";

        Result<ConversationStateResponse> send = messages.sendMessage(
                                conversationId,
                                original
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(send.isSuccess());

        assertEquals(conversationId, send.getData().conversationId());

        assertEquals(original, send.getData().lastMessagePreview());

        assertNotNull(send.getData().lastMessageAt());

        //--------------------------------------------------
        // Load Messages
        //--------------------------------------------------

        Result<ConversationMessagesResponse> loaded =
                messages.getConversationMessages(
                                conversationId,
                        3,
                        0
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(loaded.isSuccess());

        assertEquals(conversationId, loaded.getData().conversationId());

        assertEquals(1, loaded.getData().messages().size());

        MessageInfoResponse message = loaded.getData().messages().getFirst();

        assertEquals(original, message.content());

        assertEquals(conversationId, message.conversationId());

        assertEquals(userA.data().userId(), message.senderId());

        assertFalse(message.edited());

        assertFalse(message.isRead());

        assertNotNull(message.createdAt());

        UUID messageId = message.messageId();

        //--------------------------------------------------
        // Edit Message
        //--------------------------------------------------

        String edited =
                "Edited Message";

        Result<ConversationStateResponse> edit =
                messages.editMessage(
                                conversationId,
                                messageId,
                                edited
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(edit.isSuccess());

        //--------------------------------------------------
        // Get Message
        //--------------------------------------------------

        Result<MessageInfoResponse> single =
                messages.getMessage(
                                conversationId,
                                messageId
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(single.isSuccess());

        assertEquals(edited, single.getData().content());

        //--------------------------------------------------
        // Delete Message
        //--------------------------------------------------

        Result<ConversationStateResponse> deleted =
                messages.deleteMessage(
                                conversationId,
                                messageId
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(deleted.isSuccess());
    }
}
