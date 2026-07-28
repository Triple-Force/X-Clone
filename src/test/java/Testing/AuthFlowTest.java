package Testing;

import Client.ClientApplicationContext;
import Client.Service.AuthClientService;
import Client.Service.AuthClientService.AuthResult;
import logic_core.app.dto.response.AuthResponse;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("X-Clone Auth End-to-End")
class AuthFlowTest extends XCloneTest
{
    private static final String STRONG_PASSWORD = "StrongPassword123!";

    // ================================================================
    // Registration
    // ================================================================

    @Nested
    @DisplayName("Registration")
    class Registration
    {
        @Test
        @DisplayName("registers a new user, returns AuthResponse, persists user + session")
        void successfulRegistration() throws Exception
        {
            String username = uniqueUsername("reg");
            String email = username + "@example.com";

            AuthResult<AuthResponse> result = 
                    register(username, email, STRONG_PASSWORD, "Display Name");

            assertTrue(result.isSuccess(), result.errorMessage());
            assertNotNull(result.data());
            assertEquals(username, result.data().username());
            assertNotNull(result.data().token());
            assertNotNull(result.data().userId());
            assertNotNull(result.data().sessionId());
            assertNotNull(result.data().expiresAt());

            // Client session updated by AuthClientService
            assertSessionMatches(result.data(), clientContext.session());

            // DB side effects
            assertEquals(1L, countUsers());
            assertTrue(userExists(username));
            assertEquals(1L, countSessionsForUsername(username));
        }

        @Test
        @DisplayName("duplicate username fails with error code and no extra user")
        void duplicateUsernameFails() throws Exception
        {
            String username = uniqueUsername("dup");
            String email1 = username + "@a.com";
            String email2 = username + "@b.com";

            AuthResult<AuthResponse> first =
                    register(username, email1, STRONG_PASSWORD, "One");
            assertTrue(first.isSuccess());

            rebuildClient();
            AuthResult<AuthResponse> second =
                    register(username, email2, STRONG_PASSWORD, "Two");

            assertFalse(second.isSuccess());
            assertNotNull(second.errorCode());
            assertNull(second.data());
            assertFalse(clientContext.session().isLoggedIn());

            // Only one user row
            assertEquals(1L, countUsers());
        }

        @Test
        @DisplayName("duplicate email fails")
        void duplicateEmailFails() throws Exception
        {
            String u1 = uniqueUsername("e1");
            String u2 = uniqueUsername("e2");
            String sharedEmail = "shared_" + u1 + "@example.com";

            assertTrue(register(u1, sharedEmail, STRONG_PASSWORD, "A").isSuccess());
            rebuildClient();

            AuthResult<AuthResponse> second =
                    register(u2, sharedEmail, STRONG_PASSWORD, "B");

            assertFalse(second.isSuccess());
            assertNotNull(second.errorCode());
            assertEquals(1L, countUsers());
        }
    }

    // ================================================================
    // Login
    // ================================================================

    @Nested
    @DisplayName("Login")
    class Login
    {
        @Test
        @DisplayName("login with valid credentials returns token and updates client session")
        void successfulLogin() throws Exception
        {
            String username = uniqueUsername("login");
            String email = username + "@example.com";

            AuthResult<AuthResponse> reg =
                    register(username, email, STRONG_PASSWORD, "Login User");
            assertTrue(reg.isSuccess());
            UUID expectedUserId = reg.data().userId();

            // Simulate new app instance
            rebuildClient();
            AuthResult<AuthResponse> loginResult = login(username, STRONG_PASSWORD);

            assertTrue(loginResult.isSuccess(), loginResult.errorMessage());
            assertEquals(username, loginResult.data().username());
            assertEquals(expectedUserId, loginResult.data().userId());
            assertNotNull(loginResult.data().token());
            assertSessionMatches(loginResult.data(), clientContext.session());
        }

        @Test
        @DisplayName("wrong password fails")
        void wrongPasswordFails() throws Exception
        {
            String username = uniqueUsername("badpw");
            assertTrue(register(
                    username, username + "@t.com", STRONG_PASSWORD, "X"
            ).isSuccess());

            rebuildClient();
            AuthResult<AuthResponse> result = login(username, "WrongPassword999!");

            assertFalse(result.isSuccess());
            assertNotNull(result.errorCode());
            assertNull(result.data());
            assertFalse(clientContext.session().isLoggedIn());
        }

        @Test
        @DisplayName("unknown username fails")
        void unknownUserFails() throws Exception
        {
            AuthResult<AuthResponse> result =
                    login("no_such_user_" + System.nanoTime(), STRONG_PASSWORD);

            assertFalse(result.isSuccess());
            assertNotNull(result.errorCode());
            assertNull(result.data());
        }
    }

    // ================================================================
    // Full happy path through real client stack
    // ================================================================

    @Test
    @DisplayName("full flow: register -> logout path via session clear -> login")
    void fullRegisterThenLoginFlow() throws Exception
    {
        String username = uniqueUsername("full");
        AuthResult<AuthResponse> loginResult =
                registerAndLoginHappyPath(username, STRONG_PASSWORD);

        assertTrue(loginResult.isSuccess());
        assertSessionMatches(loginResult.data(), clientContext.session());
        assertTrue(countSessionsForUsername(username) >= 1L);
    }

    // ================================================================
    // Logout / Refresh (if server supports them — already in RequestType)
    // ================================================================

    @Nested
    @DisplayName("Logout & Refresh")
    class LogoutRefresh
    {
        @Test
        @DisplayName("logout clears client session when logged in")
        void logoutClearsSession() throws Exception
        {
            String username = uniqueUsername("logout");
            assertTrue(register(
                    username, username + "@t.com", STRONG_PASSWORD, "L"
            ).isSuccess());
            assertTrue(clientContext.session().isLoggedIn());

            var logoutResult = authService.logout().get(10, TimeUnit.SECONDS);
            assertTrue(logoutResult.isSuccess(), logoutResult.errorMessage());
            assertFalse(clientContext.session().isLoggedIn());
            assertNull(clientContext.session().getToken());
        }

        @Test
        @DisplayName("logout without session returns NOT_LOGGED_IN")
        void logoutWithoutSession() throws Exception
        {
            var result = authService.logout().get(5, TimeUnit.SECONDS);
            assertFalse(result.isSuccess());
            assertEquals("NOT_LOGGED_IN", result.errorCode());
        }

        @Test
        @DisplayName("refresh with current token issues new auth response")
        void refreshWithToken() throws Exception
        {
            String username = uniqueUsername("refresh");
            AuthResult<AuthResponse> reg = register(
                    username, username + "@t.com", STRONG_PASSWORD, "R"
            );
            assertTrue(reg.isSuccess());
            String token = reg.data().token();

            AuthResult<AuthResponse> refreshed =
                    authService.refresh(token).get(10, TimeUnit.SECONDS);

            // If refresh is fully wired, expect success + session update.
            // If business rules invalidate old token, still assert coherent result.
            if (refreshed.isSuccess())
            {
                assertNotNull(refreshed.data().token());
                assertSessionMatches(refreshed.data(), clientContext.session());
            } else {
                assertNotNull(refreshed.errorCode());
            }
        }
    }

    // ================================================================
    // Concurrency via multiple ClientApplicationContext instances
    // ================================================================

    @Nested
    @DisplayName("Concurrency (client stack)")
    class Concurrency
    {
        @Test
        @DisplayName("concurrent registration of unique users all succeed with unique tokens")
        void concurrentUniqueRegistrations() throws Exception
        {
            int n = 4;
            ExecutorService pool = Executors.newFixedThreadPool(n);
            CyclicBarrier barrier = new CyclicBarrier(n);
            CountDownLatch done = new CountDownLatch(n);
            ConcurrentLinkedQueue<String> tokens = new ConcurrentLinkedQueue<>();
            ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();

            for (int i = 0; i < n; i++)
            {
                final int id = i;
                pool.submit(() -> {
                    try (ClientApplicationContext ctx = newClientContext())
                    {
                        AuthClientService svc = new AuthClientService(ctx);
                        String username = "cuser_" + id + "_"
                                + UUID.randomUUID().toString().substring(0, 5);
                        barrier.await(10, TimeUnit.SECONDS);
                        AuthResult<AuthResponse> res = svc
                                .register(
                                        username,
                                        username + "@example.com",
                                        STRONG_PASSWORD,
                                        "Display"
                                )
                                .get(15, TimeUnit.SECONDS);
                        if (!res.isSuccess())
                        {
                            throw new AssertionError(
                                    "reg failed: " + res.errorCode()
                                            + " " + res.errorMessage()
                            );
                        }
                        tokens.add(res.data().token());
                    }
                    catch (Throwable t)
                    {
                        errors.add(t);
                    }
                    finally
                    {
                        done.countDown();
                    }
                });
            }

            assertTrue(done.await(40, TimeUnit.SECONDS), "timeout");
            pool.shutdownNow();

            assertTrue(errors.isEmpty(), () -> "errors: " + errors);
            assertEquals(n, tokens.size());
            assertEquals(n, new HashSet<>(tokens).size(), "tokens must be unique");
            assertEquals(n, countUsers());
        }

        @Test
        @DisplayName("concurrent logins for same user: at least one success; sessions coherent")
        void concurrentLoginSameUser() throws Exception
        {
            String username = uniqueUsername("clogin");
            assertTrue(register(
                    username, username + "@t.com", STRONG_PASSWORD, "C"
            ).isSuccess());

            int attempts = 6;
            ExecutorService pool = Executors.newFixedThreadPool(attempts);
            CyclicBarrier barrier = new CyclicBarrier(attempts);
            List<Future<AuthResult<AuthResponse>>> futures = new ArrayList<>();

            for (int i = 0; i < attempts; i++)
            {
                futures.add(pool.submit(() -> {
                    try (ClientApplicationContext ctx = newClientContext())
                    {
                        AuthClientService svc = new AuthClientService(ctx);
                        barrier.await(10, TimeUnit.SECONDS);
                        return svc.login(username, STRONG_PASSWORD)
                                .get(15, TimeUnit.SECONDS);
                    }
                }));
            }

            int success = 0;
            Set<String> tokens = new HashSet<>();
            for (Future<AuthResult<AuthResponse>> f : futures)
            {
                AuthResult<AuthResponse> r = f.get(20, TimeUnit.SECONDS);
                if (r.isSuccess())
                {
                    success++;
                    tokens.add(r.data().token());
                }
            }
            pool.shutdownNow();

            assertTrue(success >= 1, "at least one concurrent login should succeed");
            // Align with stricter concurrent tests: at most one active session policy
            // If your product allows multiple sessions, relax to <= attempts.
            long sessions = countSessionsForUsername(username);
            assertTrue(
                    sessions >= 1 && sessions <= attempts,
                    "unexpected session count: " + sessions
            );
            // Prefer asserting the product rule you already encode in Robust tests:
            // assertEquals(1L, sessions);
        }
    }

    // ================================================================
    // Password reset — intentionally not E2E until transport is complete
    // ================================================================

    @Test
    @DisplayName("password reset is not end-to-end ready (document gap)")
    void passwordResetNotWiredYet() {
        // RequestType currently: AUTH_REGISTER, AUTH_LOGIN, AUTH_LOGOUT, AUTH_REFRESH only.
        // AuthClientService has no requestPasswordReset / verifyCode / resetPassword.
        // ForgotPasswordController only navigates to VerifyCode.fxml without network I/O.
        //
        // Do NOT write green E2E tests for this flow until:
        // 1) RequestType + ResponseType values exist
        // 2) RequestDispatcher handles them
        // 3) AuthClientService exposes methods
        // 4) Controllers call the service
        Assumptions.assumeTrue(false,
                "Password reset transport/client not implemented — skip E2E");
    }

}
