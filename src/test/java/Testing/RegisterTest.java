package Testing;

import Client.Service.AuthClientService;
import Client.Service.AuthClientService.AuthResult;
import Shared.Models.Session.Session;
import Shared.Models.User.User;
import jakarta.persistence.EntityManager;
import logic_core.app.dto.response.AuthResponse;
import logic_core.common.security.PasswordHasher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class RegisterTest extends XCloneTest2
{

    @BeforeAll
    void beforeAll()
    {

        try
        {
            super.beforeAll();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setup()
    {
            super.beforeEach();
    }

    @Test
    void shouldRegisterSuccessfully() throws Exception
    {
        // Arrange

        String username = "MyUserName" ;

        String email = username + "@Mymail.com";

        String password = "REZAmoh1234!";

        String displayName = "Ali";

        AuthClientService auth =
                new AuthClientService(client);

        // Act

        AuthResult<AuthResponse> result =
                auth.register(
                        username,
                        email,
                        password,
                        displayName
                ).get(10, TimeUnit.SECONDS);

        // Assert Response

        assertNotNull(result);

        assertTrue(result.isSuccess());

        assertNull(result.errorCode());

        assertNull(result.errorMessage());

        AuthResponse response = result.data();

        assertNotNull(response);

        assertNotNull(response.userId());

        assertNotNull(response.sessionId());

        assertNotNull(response.token());

        assertEquals(username, response.username());

        // Assert Database

        assertEquals(
                1,
                db.countUsers()
        );

        assertEquals(
                1,
                db.countSessions()
        );
    }

    @Test
    void shouldRejectDuplicateUsername() throws Exception
    {
        // Arrange

        AuthClientService auth =
                new AuthClientService(client);

        // First registration

        AuthResult<AuthResponse> first =
                auth.register(
                        "MyUserName",
                        "first@mail.com",
                        "Password123!",
                        "Ali"
                ).get(10, TimeUnit.SECONDS);

        assertTrue(first.isSuccess());

        // Act

        AuthResult<AuthResponse> second =
                auth.register(
                        "MyUserName",          // duplicate username
                        "second@mail.com",
                        "Password123!",
                        "Reza"
                ).get(10, TimeUnit.SECONDS);

        // Assert Response

        assertNotNull(second);

        assertFalse(second.isSuccess());

        assertEquals(
                "AUTH_REGISTER_FAILED",
                second.errorCode()
        );

        assertNotNull(second.errorMessage());

        assertNull(second.data());

        // Assert Database

        assertEquals(
                1,
                db.countUsers()
        );

        assertEquals(
                1,
                db.countSessions()
        );
    }

    @Test
    void shouldRejectDuplicateEmail() throws Exception
    {
        // Arrange

        AuthClientService auth =
                new AuthClientService(client);

        // First registration

        AuthResult<AuthResponse> first =
                auth.register(
                        "UserOne",
                        "same@mail.com",
                        "Password123!",
                        "Ali"
                ).get(10, TimeUnit.SECONDS);

        assertTrue(first.isSuccess());

        // Act

        AuthResult<AuthResponse> second =
                auth.register(
                        "UserTwo",
                        "same@mail.com",       // duplicate email
                        "Password123!",
                        "Reza"
                ).get(10, TimeUnit.SECONDS);

        // Assert Response

        assertNotNull(second);

        assertFalse(second.isSuccess());

        assertEquals(
                "AUTH_REGISTER_FAILED",
                second.errorCode()
        );

        assertNotNull(second.errorMessage());

        assertNull(second.data());

        // Assert Database

        assertEquals(
                1,
                db.countUsers()
        );

        assertEquals(
                1,
                db.countSessions()
        );
    }

    @Test
    void shouldRejectInvalidUsername() throws Exception
    {
        // Arrange

        AuthClientService auth =
                new AuthClientService(client);

        // Act

        AuthResult<AuthResponse> result =
                auth.register(
                        "ab",                       // invalid username
                        "test@mail.com",
                        "StrongPass123!",
                        "Ali"
                ).get(10, TimeUnit.SECONDS);

        // Assert

        assertNotNull(result);

        assertFalse(result.isSuccess());

        assertNotNull(result.errorCode());

        assertNotNull(result.errorMessage());

        assertNull(result.data());

        // Database must stay unchanged

        assertEquals(0, db.countUsers());

        assertEquals(0, db.countSessions());
    }

    @Test
    void shouldRejectInvalidEmail() throws Exception
    {
        // Arrange

        AuthClientService auth =
                new AuthClientService(client);

        // Act

        AuthResult<AuthResponse> result =
                auth.register(
                        "MyUser",
                        "invalid-email",
                        "StrongPass123!",
                        "Ali"
                ).get(10, TimeUnit.SECONDS);

        // Assert

        assertNotNull(result);

        assertFalse(result.isSuccess());

        assertNotNull(result.errorCode());

        assertNotNull(result.errorMessage());

        assertNull(result.data());

        // Database must stay unchanged

        assertEquals(0, db.countUsers());

        assertEquals(0, db.countSessions());
    }


    @Test
    void shouldRejectWeakPassword() throws Exception
    {
        // Arrange

        AuthClientService auth =
                new AuthClientService(client);

        // Act

        AuthResult<AuthResponse> result =
                auth.register(
                        "MyUser",
                        "test@mail.com",
                        "1234",          // weak password
                        "Ali"
                ).get(10, TimeUnit.SECONDS);

        // Assert

        assertNotNull(result);

        assertFalse(result.isSuccess());

        assertNotNull(result.errorCode());

        assertNotNull(result.errorMessage());

        assertNull(result.data());

        // Database must stay unchanged

        assertEquals(0, db.countUsers());

        assertEquals(0, db.countSessions());
    }

    @Test
    void shouldHashPasswordBeforeSaving() throws Exception
    {
        // Arrange

        String username = "hashUser";
        String email = "hash@mail.com";
        String password = "MyPassword123!";
        String displayName = "Ali";

        AuthClientService auth =
                new AuthClientService(client);

        // Act

        AuthResult<AuthResponse> result =
                auth.register(
                        username,
                        email,
                        password,
                        displayName
                ).get(10, TimeUnit.SECONDS);

        assertTrue(result.isSuccess());

        // Assert

        EntityManager em = db.newEntityManager();

        try
        {
            User user = em.createQuery("""
                select u
                from User u
                where u.username = :username
                """, User.class)
                    .setParameter("username", username)
                    .getSingleResult();

            assertNotNull(user);

            assertNotEquals(password, user.getPasswordHash());

            assertTrue(
                    PasswordHasher.verify(
                            password,
                            user.getPasswordHash()
                    )
            );
        }
        finally
        {
            em.close();
        }
    }

    @Test
    void shouldCreateSessionForRegisteredUser() throws Exception
    {
        AuthClientService auth =
                new AuthClientService(client);

        AuthResult<AuthResponse> result =
                auth.register(
                        "sessionUser",
                        "session@mail.com",
                        "Password123!",
                        "Ali"
                ).get(10, TimeUnit.SECONDS);

        assertTrue(result.isSuccess());

        EntityManager em = db.newEntityManager();

        try
        {
            Session session = em.createQuery("""
                select s
                from Session s
                where s.user.username = :username
                """, Session.class)
                    .setParameter("username", "sessionUser")
                    .getSingleResult();

            assertNotNull(session);

            assertNotNull(session.getToken());

            assertNotNull(session.getExpiresAt());
        }
        finally
        {
            em.close();
        }
    }


    @Test
    void shouldRegisterConcurrently() throws Exception
    {
        int threadCount = 20;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch ready =
                new CountDownLatch(threadCount);

        CountDownLatch start =
                new CountDownLatch(1);

        List<Future<AuthResult<AuthResponse>>> futures =
                new ArrayList<>();

        for (int i = 0; i < threadCount; i++)
        {
            int index = i;

            futures.add(executor.submit(() ->
            {
                AuthClientService auth =
                        new AuthClientService(client);

                ready.countDown();

                start.await();

                return auth.register(
                        "user" + index,
                        "user" + index + "@mail.com",
                        "Password123!",
                        "User " + index
                ).get(10, TimeUnit.SECONDS);
            }));
        }

        ready.await();

        start.countDown();

        for (Future<AuthResult<AuthResponse>> future : futures)
        {
            AuthResult<AuthResponse> result =
                    future.get();

            assertTrue(result.isSuccess());
        }

        executor.shutdown();

        assertEquals(threadCount, db.countUsers());
        assertEquals(threadCount, db.countSessions());
    }



    @Test
    void shouldRejectConcurrentDuplicateUsername() throws Exception
    {
        int threadCount = 20;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch ready =
                new CountDownLatch(threadCount);

        CountDownLatch start =
                new CountDownLatch(1);

        List<Future<AuthResult<AuthResponse>>> futures =
                new ArrayList<>();

        for (int i = 0; i < threadCount; i++)
        {
            int index = i;

            futures.add(executor.submit(() ->
            {
                AuthClientService auth =
                        new AuthClientService(client);

                ready.countDown();

                start.await();

                return auth.register(
                        "SameUser",
                        "user" + index + "@mail.com",
                        "Password123!",
                        "Ali"
                ).get(10, TimeUnit.SECONDS);
            }));
        }

        ready.await();

        start.countDown();

        int success = 0;
        int failed = 0;

        for (Future<AuthResult<AuthResponse>> future : futures)
        {
            AuthResult<AuthResponse> result =
                    future.get();

            if (result.isSuccess())
            {
                success++;
            }
            else
            {
                failed++;
            }
        }

        executor.shutdown();

        assertEquals(1, success);
        assertEquals(threadCount - 1, failed);

        assertEquals(1, db.countUsers());
        assertEquals(1, db.countSessions());
    }

}