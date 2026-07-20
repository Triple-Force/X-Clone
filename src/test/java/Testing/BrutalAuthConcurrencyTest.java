package Testing;

import Client.ClientApplicationContext;
import Client.Service.AuthClientService;
import Client.Service.AuthClientService.AuthResult;
import logic_core.app.dto.response.AuthResponse;
import logic_core.app.dto.response.LogoutResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Brutal concurrent auth flow (register/login/logout/refresh)")
class BrutalAuthConcurrencyTest extends XCloneTest
{
    private static final String STRONG_PASSWORD = "BrutalStrongPass!42";
    private static final int USER_COUNT = 8;
    private static final int THREADS_PER_USER = 4;
    private static final int REGISTER_TIMEOUT_SEC = 30;
    private static final int AUTH_TIMEOUT_SEC = 30;
    private static final int REGISTRATION_PHASE_TIMEOUT_SEC = 60;
    private static final int AUTH_PHASE_TIMEOUT_SEC = 120;

    @Test
    @DisplayName("multi-user concurrent register/login/logout/refresh keeps DB + sessions consistent")
    void brutalConcurrentAuthFlow() throws Exception
    {
        int totalAuthTasks = USER_COUNT * THREADS_PER_USER;
        ExecutorService pool = Executors.newFixedThreadPool(totalAuthTasks);

        try
        {
            // Thread-safe containers
            List<String> usernames = new CopyOnWriteArrayList<>();
            Map<String, UUID> userIds = new ConcurrentHashMap<>();
            List<Throwable> errors = new CopyOnWriteArrayList<>();

            // 1) Concurrent registration
            List<Future<?>> registrationFutures = new ArrayList<>();
            CountDownLatch registrationsDone = new CountDownLatch(USER_COUNT);

            for (int i = 0; i < USER_COUNT; i++)
            {
                final int idx = i;
                registrationFutures.add(pool.submit(() -> {
                    String username = uniqueUsername("brutal_u" + idx);
                    String email = username + "@example.com";

                    try (ClientApplicationContext ctx = newClientContext())
                    {
                        AuthClientService svc = new AuthClientService(ctx);

                        AuthResult<AuthResponse> reg = svc.register(
                                username,
                                email,
                                STRONG_PASSWORD,
                                "User-" + idx
                        ).get(REGISTER_TIMEOUT_SEC, TimeUnit.SECONDS);

                        assertNotNull(reg, "register result must not be null for " + username);
                        assertTrue(reg.isSuccess(),
                                () -> "register failed for " + username + ": "
                                        + reg.errorCode() + " " + reg.errorMessage());

                        assertNotNull(reg.data(), "register success must carry data for " + username);
                        assertNotNull(reg.data().userId(), "registered userId must not be null for " + username);

                        usernames.add(username);
                        userIds.put(username, reg.data().userId());
                    }
                    catch (Throwable t)
                    {
                        errors.add(new AssertionError("registration failed for " + username, t));
                    }
                    finally
                    {
                        registrationsDone.countDown();
                    }
                }));
            }

            assertTrue(registrationsDone.await(REGISTRATION_PHASE_TIMEOUT_SEC, TimeUnit.SECONDS),
                    "registration phase timeout");

            for (Future<?> future : registrationFutures)
            {
                future.get(1, TimeUnit.SECONDS);
            }

            assertTrue(errors.isEmpty(), () -> "registration errors: " + errors);
            assertEquals(USER_COUNT, usernames.size(), "all usernames must be collected");
            assertEquals(USER_COUNT, userIds.size(), "all userIds must be collected");
            assertEquals(USER_COUNT, countUsers(), "DB must contain all registered users");

            // Stable ordering for deterministic execution
            List<String> orderedUsernames = List.copyOf(usernames);

            // 2) Concurrent auth flow
            List<Future<?>> authFutures = new ArrayList<>();
            CountDownLatch authOpsDone = new CountDownLatch(totalAuthTasks);

            for (String username : orderedUsernames)
            {
                for (int t = 0; t < THREADS_PER_USER; t++)
                {
                    final int attempt = t;

                    authFutures.add(pool.submit(() -> {
                        try (ClientApplicationContext ctx = newClientContext())
                        {
                            AuthClientService svc = new AuthClientService(ctx);

                            AuthResult<AuthResponse> loginRes =
                                    svc.login(username, STRONG_PASSWORD).get(AUTH_TIMEOUT_SEC, TimeUnit.SECONDS);

                            assertNotNull(loginRes, "login result must not be null for " + username);

                            if (!loginRes.isSuccess())
                            {
                                assertNotNull(loginRes.errorCode(),
                                        "failed login must expose errorCode for " + username);
                                return;
                            }

                            assertNotNull(loginRes.data(), "successful login must have data for " + username);
                            assertNotNull(loginRes.data().token(), "login token must not be null for " + username);

                            String token = loginRes.data().token();

                            // Deterministic split: even attempts refresh, odd attempts logout
                            if (attempt % 2 == 0)
                            {
                                AuthResult<AuthResponse> refreshRes =
                                        svc.refresh(token).get(AUTH_TIMEOUT_SEC, TimeUnit.SECONDS);

                                assertNotNull(refreshRes, "refresh result must not be null for " + username);
                                if (refreshRes.isSuccess())
                                {
                                    assertNotNull(refreshRes.data(), "refresh success must have data for " + username);
                                    assertNotNull(refreshRes.data().token(),
                                            "refresh token must not be null for " + username);
                                }
                                else
                                {
                                    assertNotNull(refreshRes.errorCode(),
                                            "failed refresh must expose errorCode for " + username);
                                }
                            }
                            else
                            {
                                AuthResult<LogoutResponse> logoutRes =
                                        svc.logout().get(AUTH_TIMEOUT_SEC, TimeUnit.SECONDS);

                                assertNotNull(logoutRes, "logout result must not be null for " + username);
                                if (logoutRes.isSuccess())
                                {
                                    assertFalse(ctx.session().isLoggedIn(),
                                            "context must be logged out after successful logout for " + username);
                                }
                                else
                                {
                                    assertNotNull(logoutRes.errorCode(),
                                            "failed logout must expose errorCode for " + username);
                                }
                            }
                        }
                        catch (Throwable a)
                        {
                            errors.add(new AssertionError(
                                    "auth flow failed for " + username + " [attempt=" + attempt + "]", a));
                        }
                        finally
                        {
                            authOpsDone.countDown();
                        }
                    }));
                }
            }

            assertTrue(authOpsDone.await(AUTH_PHASE_TIMEOUT_SEC, TimeUnit.SECONDS),
                    "auth ops phase timeout");

            for (Future<?> future : authFutures)
            {
                future.get(1, TimeUnit.SECONDS);
            }

            assertTrue(errors.isEmpty(), () -> "auth ops errors: " + errors);

            // 3) Stronger DB/session invariants
            for (String username : orderedUsernames)
            {
                long sessions = countSessionsForUsername(username);

                // If your product policy is "max one active session per user", keep this:
                assertTrue(sessions <= 1,
                        () -> "too many sessions for " + username + ": " + sessions);

                // If multi-session is allowed, replace with a more meaningful invariant.
                assertTrue(sessions >= 0, "session count must never be negative");
            }

            // 4) Server health after pressure
            assertServerHealthy();

        }
        finally
        {
            pool.shutdown();
            assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS),
                    "worker pool did not terminate cleanly");
            pool.shutdownNow();
        }
    }
}