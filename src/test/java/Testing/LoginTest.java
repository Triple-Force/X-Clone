package Testing;

import Client.Service.AuthClientService;
import Client.Service.AuthClientService.AuthResult;
import Shared.Models.Session.Session;
import jakarta.persistence.EntityManager;
import logic_core.app.dto.response.AuthResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class LoginTest extends XCloneTest2
{
    @BeforeAll
    void beforeAll()
    {
        try
        {
            super.beforeAll();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setup()
    {
        super.beforeEach();
    }

    private static String uniqueUsername()
    {
        // مطابق UsernameValidator:
        // 3..20 chars, only letters/numbers/_/-
        return "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 15);
    }

    private static String uniqueEmail(String username)
    {
        return username + "@test.local";
    }

    private AuthResult<AuthResponse> registerUser(
            AuthClientService auth,
            String username,
            String password
    ) throws Exception
    {
        String email = uniqueEmail(username);
        String displayName = "Test User";

        AuthResult<AuthResponse> result =
                auth.register(username, email, password, displayName)
                        .get(10, TimeUnit.SECONDS);

        assertNotNull(result);
        assertTrue(result.isSuccess(), () -> "Registration failed: " + result.errorMessage());
        assertNotNull(result.data());

        return result;
    }

    private AuthResult<AuthResponse> loginUser(
            AuthClientService auth,
            String username,
            String password
    ) throws Exception
    {
        return auth.login(username, password)
                .get(15, TimeUnit.SECONDS);
    }

    private static void assertLoginSuccess(AuthResult<AuthResponse> result, String expectedUsername)
    {
        assertNotNull(result);
        assertTrue(result.isSuccess(), () -> "Login should succeed: " + result.errorMessage());
        assertNull(result.errorCode());
        assertNull(result.errorMessage());

        assertNotNull(result.data());
        assertEquals(expectedUsername, result.data().username());
        assertNotNull(result.data().userId());
        assertNotNull(result.data().sessionId());
        assertNotNull(result.data().token());
        assertNotNull(result.data().expiresAt());
    }

    private static void assertLoginFailureDoesNotLeakIdentity(AuthResult<AuthResponse> result, String username)
    {
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.errorCode());
        assertNotNull(result.errorMessage());
        assertNull(result.data());

        // نباید اطلاعات کاربر را لو بدهد
        assertFalse(result.errorMessage().toLowerCase().contains(username.toLowerCase()));
    }

    @Test
    void shouldLoginSuccessfully() throws Exception
    {
        AuthClientService auth = new AuthClientService(client);

        String username = uniqueUsername();
        String password = "Password123!";

        registerUser(auth, username, password);

        AuthResult<AuthResponse> result = loginUser(auth, username, password);

        assertLoginSuccess(result, username);
    }

    @Test
    void shouldFailLoginWithWrongPasswordWithoutLeakingUserInfo() throws Exception
    {
        AuthClientService auth = new AuthClientService(client);

        String username = uniqueUsername();
        String password = "Password123!";
        String wrongPassword = "WrongPassword123!";

        registerUser(auth, username, password);

        AuthResult<AuthResponse> result = loginUser(auth, username, wrongPassword);

        assertLoginFailureDoesNotLeakIdentity(result, username);
    }

    @Test
    void shouldFailLoginForNonExistentUserWithoutLeakingUserInfo() throws Exception
    {
        AuthClientService auth = new AuthClientService(client);

        String username = uniqueUsername();
        String password = "Password123!";

        AuthResult<AuthResponse> result = loginUser(auth, username, password);

        assertLoginFailureDoesNotLeakIdentity(result, username);
    }

    @Test
    void shouldSupportConcurrentLogins() throws Exception
    {
        AuthClientService auth = new AuthClientService(client);

        String username = uniqueUsername();
        String password = "Password123!";

        registerUser(auth, username, password);

        int threadCount = 12;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        try
        {
            CountDownLatch startGate = new CountDownLatch(1);
            List<Future<AuthResult<AuthResponse>>> futures = new ArrayList<>();

            for (int i = 0; i < threadCount; i++)
            {
                futures.add(executor.submit(() ->
                {
                    startGate.await(10, TimeUnit.SECONDS);
                    AuthClientService concurrentAuth = new AuthClientService(client);
                    return concurrentAuth.login(username, password).get(15, TimeUnit.SECONDS);
                }));
            }

            startGate.countDown();

            for (Future<AuthResult<AuthResponse>> future : futures)
            {
                AuthResult<AuthResponse> result = future.get(20, TimeUnit.SECONDS);
                assertLoginSuccess(result, username);
            }
        }
        finally
        {
            executor.shutdownNow();
        }
    }

    @Test
    void shouldReplacePreviousSessionOnLogin() throws Exception
    {
        // Arrange

        AuthClientService auth =
                new AuthClientService(client);

        String username = "reza";
        String email = "reza@mail.com";
        String password = "Password123!";

        // Register

        AuthResult<AuthResponse> register =
                auth.register(
                        username,
                        email,
                        password,
                        "Reza"
                ).get(10, TimeUnit.SECONDS);

        assertTrue(register.isSuccess());

        String firstToken =
                register.data().token();

        UUID firstSessionId =
                register.data().sessionId();

        // Act

        AuthResult<AuthResponse> login =
                auth.login(
                        username,
                        password
                ).get(10, TimeUnit.SECONDS);

        // Assert response

        assertTrue(login.isSuccess());

        String secondToken =
                login.data().token();

        UUID secondSessionId =
                login.data().sessionId();

        assertNotEquals(firstToken, secondToken);
        assertNotEquals(firstSessionId, secondSessionId);

        // Database

        assertEquals(1, db.countSessions());

        EntityManager em = db.newEntityManager();

        try
        {
            Session session = em.createQuery("""
                select s
                from Session s
                where s.user.username = :username
                """, Session.class)
                    .setParameter("username", username)
                    .getSingleResult();

            assertEquals(secondSessionId, session.getId());
            assertEquals(secondToken, session.getToken());
        }
        finally
        {
            em.close();
        }
    }
}
