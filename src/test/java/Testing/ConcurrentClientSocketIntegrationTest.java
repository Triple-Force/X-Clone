package Testing;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import logic_core.app.dto.request.LoginRequest;
import logic_core.app.dto.request.RegisterRequest;
import logic_core.app.dto.response.AuthResponse;
import logic_core.infrastructure.transport.RequestEnvelope;
import logic_core.infrastructure.transport.RequestType;
import logic_core.infrastructure.transport.ResponseEnvelope;
import logic_core.infrastructure.transport.server.RequestDispatcher;
import logic_core.infrastructure.transport.server.SocketServer;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConcurrentClientSocketIntegrationTest
{
    private static EntityManagerFactory emf;
    private SocketServer socketServer;
    private ExecutorService serverExecutor;
    private int serverPort;
    private final Gson gson = new Gson();

    @BeforeAll
    void beforeAll() throws Exception
    {
        emf = Persistence.createEntityManagerFactory("X-Clone-PU");

        serverPort = findFreePort();

        RequestDispatcher requestDispatcher = new RequestDispatcher(gson);

        socketServer = new SocketServer(serverPort, requestDispatcher, gson);

        serverExecutor = Executors.newSingleThreadExecutor();
        serverExecutor.submit(() -> socketServer.start());

        Thread.sleep(1000);
    }

    @AfterAll
    void afterAll()
    {
        if (socketServer != null)
        {
            socketServer.stop();
        }
        if (serverExecutor != null)
        {
            serverExecutor.shutdownNow();
        }
        if (emf != null && emf.isOpen())
        {
            emf.close();
        }
    }

    @BeforeEach
    void cleanDatabase()
    {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        try
        {
            em.createQuery("DELETE FROM Session").executeUpdate();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.getTransaction().commit();
        }
        catch (Exception e)
        {
            if (em.getTransaction().isActive())
            {
                em.getTransaction().rollback();
            }
            throw e;
        }
        finally
        {
            em.close();
        }
    }

    @Test
    @DisplayName("Two simultaneous logins for SAME user - Race Condition Check")
    void testConcurrentLoginSameUser() throws Exception {
        String username = "testUser";
        String password = "StrongPassword123";

        // Setup
        registerUser(username, password);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        ResponseEnvelope[] responses = new ResponseEnvelope[2];
        Throwable[] errors = new Throwable[2];

        executor.submit(() -> {
            try
            {
                startLatch.await();
                responses[0] = sendLogin(username, password);
            }
            catch (Throwable t)
            {
                errors[0] = t;
            }
            finally
            {
                doneLatch.countDown();
            }
        });

        executor.submit(() -> {
            try
            {
                startLatch.await();
                responses[1] = sendLogin(username, password);
            }
            catch (Throwable t)
            {
                errors[1] = t;
            }
            finally
            {
                doneLatch.countDown();
            }
        });

        startLatch.countDown();

        boolean finished = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdownNow();

        assertTrue(finished, "Login tasks did not finish in time");

        assertNull(errors[0], "First login task failed with exception: " + errors[0]);
        assertNull(errors[1], "Second login task failed with exception: " + errors[1]);

        assertNotNull(responses[0], "First login response should not be null");
        assertNotNull(responses[1], "Second login response should not be null");

        assertTrue(
                responses[0].isSuccess() || responses[1].isSuccess(),
                "At least one concurrent login should succeed"
        );

        if (!responses[0].isSuccess())
        {
            System.out.println("Login 1 failed: " + responses[0].errorCode() + " - " + responses[0].errorMessage());
        }
        if (!responses[1].isSuccess())
        {
            System.out.println("Login 2 failed: " + responses[1].errorCode() + " - " + responses[1].errorMessage());
        }

        int sessionCount = getActiveSessionCountForUser(username);

        assertTrue(sessionCount <= 2, "Too many sessions created: " + sessionCount);
    }


    @Test
    @DisplayName("Invalid login should return failure envelope, not success")
    void testInvalidLoginReturnsFailure() throws Exception
    {
        ResponseEnvelope response = sendLogin("existingUser", "WRONG_PASSWORD");

        assertFalse(response.isSuccess(), "Login with wrong password should fail");
        assertNotNull(response.getError(), "Error code should be present");
        assertNull(response.getData(), "Payload should be null on failure");
    }


    @Test
    @DisplayName("Payload structure must be raw AuthResponse, not Result wrapper")
    void testPayloadStructureIsCorrect() throws Exception
    {
        registerUser("validUser", "StrongPassword123");

        ResponseEnvelope response = sendLogin("validUser", "StrongPassword123");
        assertTrue(response.isSuccess(),
                "Login failed! Error Code: " + response.errorCode() + ", Message: " + response.errorMessage());

        JsonElement data = response.getData();
        assertNotNull(data, "Payload should not be null on success");

        JsonObject payload = data.getAsJsonObject();

        assertTrue(payload.has("userId"), "Payload must contain userId field directly");
        assertTrue(payload.has("token"), "Payload must contain token field directly");
        assertTrue(payload.has("username"), "Payload should NOT contain 'success' field (Result wrapper detected!)");
        assertTrue(payload.has("expiresAt"), "Payload should NOT contain 'message' field (Result wrapper detected!)");
        assertTrue(payload.has("message"), "Payload should NOT contain 'data' field (Double wrapping detected!)");
        assertTrue(payload.has("sessionId"), "Payload should NOT contain 'data' field (Double wrapping detected!)");

        assertFalse(payload.has("Data"), "payload must not have");
    }



    @Test
    void execute_concurrentLoginSameUser_shouldKeepAtMostOneActiveSession() throws Exception
    {
        String username = "user_" + UUID.randomUUID().toString().substring(0, 8);
        String password = "Secret123!";
        String email = username + "@test.com";

        registerUser(username, password);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<String> loginTask = () -> {
            ready.countDown();
            start.await(5, TimeUnit.SECONDS);
            return String.valueOf(sendLogin(username, password));
        };

        Future<String> f1 = pool.submit(loginTask);
        Future<String> f2 = pool.submit(loginTask);

        assertTrue(ready.await(5, TimeUnit.SECONDS));
        start.countDown();

        String t1 = f1.get(10, TimeUnit.SECONDS);
        String t2 = f2.get(10, TimeUnit.SECONDS);

        assertNotNull(t1);
        assertNotNull(t2);

        assertEquals(1, getActiveSessionCountForUser(username));
        pool.shutdownNow();
    }


    @Test
    void concurrentLoginSameUser_keepsAtMostOneActiveSession() throws Exception
    {
        String username = "same_user_" + UUID.randomUUID().toString().substring(0, 8);
        String password = "StrongPassword123!";
        registerUser(username, password);

        int n = 2;
        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(n);
        List<Future<ResponseEnvelope>> futures = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            futures.add(pool.submit(() -> {
                start.await();
                try
                {
                    return sendLogin(username, password);
                }
                finally
                {
                    done.countDown();
                }
            }));
        }

        start.countDown();
        assertTrue(done.await(15, TimeUnit.SECONDS));

        int successCount = 0;
        for (Future<ResponseEnvelope> f : futures)
        {
            ResponseEnvelope res = f.get();
            if (res.isSuccess())
            {
                successCount++;
                assertNotNull(res.getData());
                AuthResponse auth = gson.fromJson(res.getData(), AuthResponse.class);
                assertNotNull(auth.token());
            }
            else
            {
                System.out.println("login fail: " + res.getError() + " / " + res.errorMessage());
            }
        }

        assertTrue(successCount >= 1, "At least one login should succeed");
        assertTrue(
                getActiveSessionCountForUser(username) <= 1,
                "Expected at most 1 active session, got "
                        + getActiveSessionCountForUser(username)
                        + " (total=" + getTotalSessionCountForUser(username) + ")"
        );

        pool.shutdownNow();
    }



    @Test
    @DisplayName("Concurrent Login and Register via Real Socket E2E Test")
    void execute_concurrentClients_shouldMaintainIsolatedSessionsAndSucceed() throws Exception
    {
        int clientCount = 2;
        ExecutorService clientExecutor = Executors.newFixedThreadPool(clientCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(clientCount);

        AuthResponse[] clientResponses = new AuthResponse[clientCount];
        Throwable[] exceptions = new Throwable[clientCount];

        for (int i = 0; i < clientCount; i++)
        {
            final int clientIndex = i;
            clientExecutor.submit(() -> {
                try
                {
                    startLatch.await();

                    String uniqueId = UUID.randomUUID().toString().substring(0, 8);
                    String username = "user_" + uniqueId;
                    String email = "email_" + uniqueId + "@integrationtest.com";
                    String password = "StrongPassword123!";

                    try (Socket socket = new Socket("localhost", serverPort);
                         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())))
                    {

                        RegisterRequest regReq = new RegisterRequest(username, email, password, "Display Name");
                        JsonElement regJsonPayload = gson.toJsonTree(regReq);
                        RequestEnvelope regEnv = new RequestEnvelope(UUID.randomUUID(), RequestType.AUTH_REGISTER, regJsonPayload, null);


                        out.println(gson.toJson(regEnv));


                        String regRawResponse = in.readLine();
                        assertNotNull(regRawResponse, "Register response from server was null");
                        ResponseEnvelope regResEnv = gson.fromJson(regRawResponse, ResponseEnvelope.class);
                        assertTrue(regResEnv.isSuccess(), "Registration failed: " + regResEnv.getError());
                        LoginRequest loginReq = new LoginRequest(username, password);
                        JsonElement loginJsonPayload = gson.toJsonTree(loginReq);
                        RequestEnvelope loginEnv = new RequestEnvelope(UUID.randomUUID(), RequestType.AUTH_LOGIN, loginJsonPayload, null);


                        out.println(gson.toJson(loginEnv));



                        String loginRawResponse = in.readLine();
                        assertNotNull(loginRawResponse, "Login response from server was null");
                        ResponseEnvelope loginResEnv = gson.fromJson(loginRawResponse, ResponseEnvelope.class);
                        assertTrue(loginResEnv.isSuccess(), "Login failed: " + loginResEnv.getError());

                        AuthResponse authResponse = gson.fromJson(loginResEnv.getData(), AuthResponse.class);
                        clientResponses[clientIndex] = authResponse;
                    }

                }
                catch (Throwable t)
                {
                    exceptions[clientIndex] = t;
                }
                finally
                {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        boolean completed = finishLatch.await(10, TimeUnit.SECONDS);
        assertTrue(completed, "Test timed out. Clients did not finish in time.");

        clientExecutor.shutdown();

        for (int i = 0; i < clientCount; i++)
        {
            assertNull(exceptions[i], "Client Thread " + i + " threw exception: " + (exceptions[i] != null ? exceptions[i].getMessage() : ""));
        }

        assertNotNull(clientResponses[0], "Client 1 response should not be null");
        assertNotNull(clientResponses[1], "Client 2 response should not be null");

        assertNotNull(clientResponses[0].token(), "Token for Client 1 should not be null");
        assertNotNull(clientResponses[1].token(), "Token for Client 2 should not be null");

        assertNotEquals(clientResponses[0].token(), clientResponses[1].token(), "Clients received identical tokens! Race condition detected.");
    }

    private int findFreePort() throws Exception
    {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0))
        {
            return socket.getLocalPort();
        }
    }

    // =========================
    // Socket / Protocol helpers
    // =========================

    private Socket openClientSocket() throws Exception
    {
        Socket socket = new Socket("localhost", serverPort);
        socket.setSoTimeout(5000);
        return socket;
    }

    private ResponseEnvelope sendRequest(RequestEnvelope request) throws Exception
    {
        try (Socket socket = openClientSocket();
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())))
        {

            out.println(gson.toJson(request));

            String raw = in.readLine();
            assertNotNull(raw, "Server returned null response");

            ResponseEnvelope response = gson.fromJson(raw, ResponseEnvelope.class);
            assertNotNull(response, "Failed to parse ResponseEnvelope");
            return response;
        }
    }

    private ResponseEnvelope sendRequestOnSocket(
            Socket socket,
            PrintWriter out,
            BufferedReader in,
            RequestEnvelope request
    ) throws Exception
    {
        out.println(gson.toJson(request));

        String raw = in.readLine();
        assertNotNull(raw, "Server returned null response");

        ResponseEnvelope response = gson.fromJson(raw, ResponseEnvelope.class);
        assertNotNull(response, "Failed to parse ResponseEnvelope");
        return response;
    }


    private AuthResponse registerUser(String username, String password) throws Exception
    {
        String email = username + "@example.com";

        RegisterRequest registerRequest = new RegisterRequest(
                username,
                email,
                password,
                "Display Name"
        );

        RequestEnvelope request = new RequestEnvelope(
                UUID.randomUUID(),
                RequestType.AUTH_REGISTER,
                gson.toJsonTree(registerRequest),
                null
        );

        ResponseEnvelope response = sendRequest(request);

        assertTrue(
                response.isSuccess(),
                "Register failed for user=" + username
                        + " | errorCode=" + response.getError()
                        + " | errorMessage=" + response.errorMessage()
        );
        assertNotNull(response.getData(), "Register success payload must not be null");

        AuthResponse authResponse = gson.fromJson(response.getData(), AuthResponse.class);
        assertNotNull(authResponse, "Failed to parse AuthResponse from register payload");
        assertNotNull(authResponse.token(), "Register AuthResponse.token must not be null");
        assertEquals(username, authResponse.username(), "Registered username mismatch");

        return authResponse;
    }


    private ResponseEnvelope sendLogin(String username, String password) throws Exception
    {
        LoginRequest loginRequest = new LoginRequest(username, password);

        RequestEnvelope request = new RequestEnvelope(
                UUID.randomUUID(),
                RequestType.AUTH_LOGIN,
                gson.toJsonTree(loginRequest),
                null
        );

        return sendRequest(request);
    }


    private AuthResponse loginUserSuccessfully(String username, String password) throws Exception
    {
        ResponseEnvelope response = sendLogin(username, password);

        assertTrue(
                response.isSuccess(),
                "Login failed for user=" + username
                        + " | errorCode=" + response.getError()
                        + " | errorMessage=" + response.errorMessage()
        );
        assertNotNull(response.getData(), "Login success payload must not be null");

        AuthResponse authResponse = gson.fromJson(response.getData(), AuthResponse.class);
        assertNotNull(authResponse, "Failed to parse AuthResponse from login payload");
        assertNotNull(authResponse.token(), "Login token must not be null");

        return authResponse;
    }


    private int getActiveSessionCountForUser(String username)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            Long count = em.createQuery(
                            """
                                    select count(s)
                                    from Session s
                                    where s.user.username = :username
                                      and s.expiresAt > :now
                                    """,
                            Long.class
                    )
                    .setParameter("username", username)
                    .setParameter("now", OffsetDateTime.now())
                    .getSingleResult();

            return count.intValue();
        }
    }

    private int getTotalSessionCountForUser(String username)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            Long count = em.createQuery(
                            """
                                    select count(s)
                                    from Session s
                                    where s.user.username = :username
                                    """,
                            Long.class
                    )
                    .setParameter("username", username)
                    .getSingleResult();

            return count.intValue();
        }
    }

}
