package logic_core.infrastructure.transport.http;

import com.google.gson.Gson;
import logic_core.app.dto.request.RegisterRequest;
import logic_core.app.dto.request.RequestPasswordResetRequest;
import logic_core.app.dto.request.ResetPasswordRequest;
import logic_core.app.dto.request.VerifyPasswordResetCodeRequest;
import logic_core.app.dto.response.AuthResponse;
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
 * Integration tests for password-reset failure propagation in the transport
 * layer.
 *
 * <p>Previously {@code handelRequestPasswordReset} /
 * {@code handelVerifyPasswordResetCode} / {@code handelResetPassword} ignored
 * {@code Result.isFailure()} and always returned a success envelope. These tests
 * prove that use-case failures now produce the standard failure envelope while
 * success responses are preserved.
 */
@SpringBootTest(classes = ServerMain.class)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PasswordResetFailurePropagationIntegrationTest {

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
        try {
            if (!createdUserIds.isEmpty()) {
                String placeholders = repeatPlaceholders(createdUserIds.size());
                Object[] userArgs = createdUserIds.toArray();
                jdbcTemplate.update(
                        "DELETE FROM sessions WHERE user_id IN (" + placeholders + ")", userArgs);
                jdbcTemplate.update(
                        "DELETE FROM users WHERE id IN (" + placeholders + ")", userArgs);
            }
        } catch (Exception e) {
            System.err.println("PasswordResetFailurePropagationIntegrationTest cleanup warning: " + e.getMessage());
        }
    }

    @Test
    void requestPasswordReset_validRegisteredEmail_returnsSuccessEnvelope() throws Exception {
        String username = "prta_" + UUID.randomUUID().toString().substring(0, 8);
        String email = username + "@passwordtest.com";
        registerUser(username, email);

        ResponseEnvelope envelope = sendRequestReset(email);
        assertSuccess(envelope, "request reset for registered email");
    }

    @Test
    void requestPasswordReset_malformedEmail_returnsFailureEnvelope() throws Exception {
        // Invalid email fails in the use case (EmailValidator); the dispatcher
        // must convert the Result failure into a failure envelope.
        ResponseEnvelope envelope = sendRequestReset("not-an-email");
        assertThat(envelope.isSuccess())
                .as("malformed email must yield a failure envelope — error=%s/%s",
                        envelope.errorCode(), envelope.errorMessage())
                .isFalse();
        assertThat(envelope.errorCode()).isEqualTo("AUTH_REQUEST_PASSWORD_RESET_FAILED");
    }

    @Test
    void verifyPasswordResetCode_malformedEmail_returnsFailureEnvelope() throws Exception {
        VerifyPasswordResetCodeRequest request =
                new VerifyPasswordResetCodeRequest("not-an-email", "123456");
        ResponseEnvelope envelope = send(new RequestEnvelope(
                UUID.randomUUID(),
                RequestType.AUTH_VERIFY_PASSWORD_RESET_CODE,
                gson.toJsonTree(request),
                null));
        assertThat(envelope.isSuccess())
                .as("malformed email verify must yield a failure envelope — error=%s/%s",
                        envelope.errorCode(), envelope.errorMessage())
                .isFalse();
        assertThat(envelope.errorCode()).isEqualTo("AUTH_VERIFY_PASSWORD_RESET_CODE_FAILED");
    }

    @Test
    void resetPassword_invalidPassword_returnsFailureEnvelope() throws Exception {
        // ResetPasswordUseCase validates the new password format up front, so no
        // valid OTP is needed to exercise the failure path.
        ResetPasswordRequest request =
                new ResetPasswordRequest("someone@example.com", "000000", "short");
        ResponseEnvelope envelope = send(new RequestEnvelope(
                UUID.randomUUID(),
                RequestType.AUTH_RESET_PASSWORD,
                gson.toJsonTree(request),
                null));
        assertThat(envelope.isSuccess())
                .as("invalid new password must yield a failure envelope — error=%s/%s",
                        envelope.errorCode(), envelope.errorMessage())
                .isFalse();
        assertThat(envelope.errorCode()).isEqualTo("AUTH_RESET_PASSWORD_FAILED");
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private AuthResponse registerUser(String username, String email) throws Exception {
        String prefix = username.split("_")[0];
        RegisterRequest registerRequest =
                new RegisterRequest(username, email, "StrongPassword123!", "Display " + prefix);

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

    private ResponseEnvelope sendRequestReset(String email) throws Exception {
        RequestPasswordResetRequest request = new RequestPasswordResetRequest(email);
        return send(new RequestEnvelope(
                UUID.randomUUID(),
                RequestType.AUTH_REQUEST_PASSWORD_RESET,
                gson.toJsonTree(request),
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
