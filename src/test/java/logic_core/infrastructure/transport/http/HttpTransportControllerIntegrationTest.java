package logic_core.infrastructure.transport.http;

import com.google.gson.Gson;
import logic_core.app.dto.request.GetProfileRequest;
import logic_core.app.dto.request.RegisterRequest;
import logic_core.app.dto.response.AuthResponse;
import logic_core.app.dto.response.ProfileInfoResponse;
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
 * HTTP Transport Phase 1 integration test.
 *
 * <p>Boots the real Spring application ({@link ServerMain}) and drives the new
 * {@code POST /api} endpoint with the exact same {@link RequestEnvelope} JSON the
 * socket transport sends, verifying that requests reach the existing
 * {@code RequestDispatcher} → Facade/UseCase stack and that the returned body is a
 * {@link ResponseEnvelope} serialized with the same Gson semantics as the socket.
 *
 * <p>Authentication is intentionally untouched in this phase: the authenticated
 * request below carries {@code sessionToken} inside the payload DTO, exactly like
 * the socket transport does today.
 */
@SpringBootTest(classes = ServerMain.class)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpTransportControllerIntegrationTest {

    // The socket server is still started by Spring (@PostConstruct) and must not
    // collide with anything else; give it a free port just like the socket tests do.
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

    private final List<UUID> createdUserIds = new ArrayList<>();

    @AfterEach
    void cleanUpCreatedRows() {
        // Best-effort cleanup of rows created by this test (random usernames make
        // leftovers harmless, but keeping the dev DB tidy is nicer).
        try {
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
            System.err.println("HttpTransportControllerIntegrationTest cleanup warning: " + e.getMessage());
        }
    }

    // ========================================================================
    // Tests
    // ========================================================================

    @Test
    void malformedJson_returnsFailureEnvelope() throws Exception {
        MockHttpServletResponse response =
                sendPost("/api", "this is not json", MediaType.APPLICATION_JSON);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentType())
                .as("response must be JSON")
                .contains(MediaType.APPLICATION_JSON_VALUE);

        ResponseEnvelope envelope = gson.fromJson(response.getContentAsString(), ResponseEnvelope.class);
        assertThat(envelope).isNotNull();
        assertThat(envelope.isSuccess()).isFalse();
        assertThat(envelope.type()).isEqualTo("BAD_REQUEST");
        assertThat(envelope.errorCode()).isEqualTo("MALFORMED_JSON");
        assertThat(envelope.errorMessage()).contains("Invalid payload format:");
    }

    @Test
    void register_publicRequest_reachesDispatcher_andPreservesRequestId() throws Exception {
        String username = "httpa_" + UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest registerRequest =
                new RegisterRequest(username, username + "@integrationtest.com", "StrongPassword123!", "HTTP A");

        RequestEnvelope request = new RequestEnvelope(
                UUID.randomUUID(),
                RequestType.AUTH_REGISTER,
                gson.toJsonTree(registerRequest),
                null
        );

        MockHttpServletResponse response = sendPost("/api", gson.toJson(request), MediaType.APPLICATION_JSON);
        assertThat(response.getStatus()).isEqualTo(200);

        ResponseEnvelope envelope = gson.fromJson(response.getContentAsString(), ResponseEnvelope.class);
        assertThat(envelope).isNotNull();
        assertThat(envelope.isSuccess())
                .as("register must succeed — errorCode=%s, errorMessage=%s",
                        envelope.errorCode(), envelope.errorMessage())
                .isTrue();
        assertThat(envelope.type()).isEqualTo("AUTH_REGISTER_RESPONSE");

        // Request/response correlation is preserved.
        assertThat(envelope.requestId()).isEqualTo(request.requestId());

        // The payload deserializes into the same DTO the socket clients consume.
        AuthResponse auth = gson.fromJson(envelope.getData(), AuthResponse.class);
        assertThat(auth.userId()).isNotNull();
        assertThat(auth.token()).isNotBlank();
        assertThat(auth.sessionId()).isNotNull();
        createdUserIds.add(auth.userId());
    }

    @Test
    void authenticatedRequest_withPayloadSessionToken_reachesApplicationStack() throws Exception {
        AuthResponse auth = registerUser("httpb");

        // USER_GET_PROFILE validates the payload sessionToken via the existing
        // application-layer auth path (AuthLockOrchestrator), exactly like socket.
        GetProfileRequest profileRequest =
                new GetProfileRequest(auth.token(), auth.userId());

        RequestEnvelope request = new RequestEnvelope(
                UUID.randomUUID(),
                RequestType.USER_GET_PROFILE,
                gson.toJsonTree(profileRequest),
                null
        );

        MockHttpServletResponse response = sendPost("/api", gson.toJson(request), MediaType.APPLICATION_JSON);
        assertThat(response.getStatus()).isEqualTo(200);

        ResponseEnvelope envelope = gson.fromJson(response.getContentAsString(), ResponseEnvelope.class);
        assertThat(envelope).isNotNull();
        assertThat(envelope.isSuccess())
                .as("authenticated request must succeed — errorCode=%s, errorMessage=%s",
                        envelope.errorCode(), envelope.errorMessage())
                .isTrue();
        assertThat(envelope.type()).isEqualTo("USER_GET_PROFILE_RESPONSE");
        assertThat(envelope.requestId()).isEqualTo(request.requestId());

        ProfileInfoResponse profile = gson.fromJson(envelope.getData(), ProfileInfoResponse.class);
        assertThat(profile.userId()).isEqualTo(auth.userId());
        assertThat(profile.username()).isEqualTo(auth.username());
    }

    @Test
    void unknownRequestType_returnsMalformedJsonFailure() throws Exception {
        MockHttpServletResponse response =
                sendPost("/api", "{\"type\":\"NOT_A_REAL_TYPE\",\"payload\":{}}", MediaType.APPLICATION_JSON);
        assertThat(response.getStatus()).isEqualTo(200);

        ResponseEnvelope envelope = gson.fromJson(response.getContentAsString(), ResponseEnvelope.class);
        assertThat(envelope).isNotNull();
        assertThat(envelope.isSuccess()).isFalse();
        assertThat(envelope.errorCode()).isEqualTo("MALFORMED_JSON");
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private MockHttpServletResponse sendPost(String path, String body, MediaType contentType) throws Exception {
        return mockMvc.perform(post(path)
                        .contentType(contentType)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
    }

    private AuthResponse registerUser(String prefix) throws Exception {
        String username = prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
        String email = username + "@integrationtest.com";
        String password = "StrongPassword123!";

        RegisterRequest registerRequest =
                new RegisterRequest(username, email, password, "Display " + prefix);

        RequestEnvelope request = new RequestEnvelope(
                UUID.randomUUID(),
                RequestType.AUTH_REGISTER,
                gson.toJsonTree(registerRequest),
                null
        );

        MockHttpServletResponse response = sendPost("/api", gson.toJson(request), MediaType.APPLICATION_JSON);

        ResponseEnvelope envelope = gson.fromJson(response.getContentAsString(), ResponseEnvelope.class);
        assertThat(envelope).isNotNull();
        assertThat(envelope.isSuccess())
                .as("register must succeed — errorCode=%s, errorMessage=%s",
                        envelope.errorCode(), envelope.errorMessage())
                .isTrue();

        AuthResponse auth = gson.fromJson(envelope.getData(), AuthResponse.class);
        assertThat(auth.userId()).isNotNull();
        assertThat(auth.token()).isNotBlank();
        createdUserIds.add(auth.userId());
        return auth;
    }

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (Exception e) {
            throw new IllegalStateException("Could not find a free TCP port", e);
        }
    }
}