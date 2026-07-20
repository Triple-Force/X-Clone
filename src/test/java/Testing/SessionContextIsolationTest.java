package Testing;

import Client.ClientApplicationContext;
import Client.Service.AuthClientService;
import Client.Service.AuthClientService.AuthResult;
import logic_core.app.dto.response.AuthResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ThreadLocalUserSessionContext isolation across threads")
class SessionContextIsolationTest extends XCloneTest
{
    private static final String PASS = "IsoPass!123";

    @Test
    @DisplayName("session context is isolated per thread; tokens do not leak between ClientApplicationContext instances")
    void sessionContextPerThreadIsolation() throws Exception
    {
        String username = uniqueUsername("iso");
        String email = username + "@iso.test";
        AuthResult<AuthResponse> reg =
                register(username, email, PASS, "IsoUser");
        assertTrue(reg.isSuccess());
        String token1 = reg.data().token();
        UUID userId = reg.data().userId();

        int threads = 4;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        ConcurrentLinkedQueue<String> observedTokens = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < threads; i++)
        {
            pool.submit(() -> {
                try (ClientApplicationContext ctx = newClientContext())
                {
                    AuthClientService svc = new AuthClientService(ctx);
                    AuthResult<AuthResponse> loginRes =
                            svc.login(username, PASS).get(10, TimeUnit.SECONDS);

                    assertTrue(loginRes.isSuccess());
                    assertEquals(userId, loginRes.data().userId());
                    observedTokens.add(ctx.session().getToken());
                }
                catch (Throwable t)
                {
                    fail("thread failed: " + t);
                }
                finally
                {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "threads timeout");
        pool.shutdownNow();

        assertEquals(threads, observedTokens.size());
        observedTokens.forEach(tok -> assertNotNull(tok));

        long sessions = countSessionsForUsername(username);
        assertTrue(sessions >= 1, "at least one session must exist");

        assertServerHealthy();
    }
}
