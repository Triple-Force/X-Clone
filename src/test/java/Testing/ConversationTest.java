package Testing;

import Client.Service.AuthClientService;
import Client.Service.ConversationClientService;
import logic_core.app.dto.response.AuthResponse;
import logic_core.app.dto.response.CreateConversationResponse;
import logic_core.common.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


import static org.junit.jupiter.api.Assertions.*;

public class ConversationTest extends XCloneTest2
{
    private static final long TIMEOUT_SECONDS = 10;

    @Test
    @DisplayName("should create conversation successfully")
    void shouldCreateConversation() throws Exception
    {
        AuthClientService auth =
                new AuthClientService(client);

        ConversationClientService conversation = new ConversationClientService(client);

        //--------------------------------------------------
        // Register first user
        //--------------------------------------------------

        String ownerUsername =
                "owner_" + UUID.randomUUID().toString().substring(0, 8);

        AuthClientService.AuthResult<AuthResponse> owner =
                auth.register(
                                ownerUsername,
                                ownerUsername + "@example.com",
                                "Password123!",
                                "Owner"
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(owner.isSuccess());

        //--------------------------------------------------
        // Register second user
        //--------------------------------------------------

        rebuildClient();

        auth = new AuthClientService(client);

        String memberUsername =
                "member_" + UUID.randomUUID().toString().substring(0, 8);

        AuthClientService.AuthResult<AuthResponse> member =
                auth.register(
                                memberUsername,
                                memberUsername + "@example.com",
                                "Password123!",
                                "Member"
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(member.isSuccess());

        //--------------------------------------------------
        // Login owner again
        //--------------------------------------------------

        rebuildClient();

        auth = new AuthClientService(client);

        conversation =
                new ConversationClientService(client);

        assertTrue(
                auth.login(
                                ownerUsername,
                                "Password123!"
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .isSuccess()
        );

        //--------------------------------------------------
        // Create Conversation
        //--------------------------------------------------

        Result<CreateConversationResponse> create =
                conversation.createConversation(
                                owner.data().userId(),
                                List.of(member.data().userId())
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(create.isSuccess(), create.getError());

        assertNotNull(create.getData());
        assertNotNull(create.getData().conversationId());
        assertNotNull(create.getData());

        assertNotNull(
                create.getData().conversationId()
        );
    }
}
