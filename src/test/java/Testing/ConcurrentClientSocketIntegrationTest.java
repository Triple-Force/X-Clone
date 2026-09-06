package Testing;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import logic_core.app.dto.request.LoginRequest;
import logic_core.app.dto.request.RegisterRequest;
import logic_core.app.dto.response.AuthResponse;
import logic_core.infrastructure.transport.RequestEnvelope;
import logic_core.infrastructure.transport.RequestType;
import logic_core.infrastructure.transport.ResponseEnvelope;
import logic_core.infrastructure.transport.server.ServerMain;
import logic_core.infrastructure.transport.server.SocketServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;


/**
 * End-to-end integration tests for the TCP socket transport layer.
 *
 * The test starts the real Spring Boot application context and therefore uses
 * the real Spring-managed SocketServer, RequestDispatcher, Facades, UseCases
 * and persistence infrastructure.
 */
@SpringBootTest(classes = ServerMain.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConcurrentClientSocketIntegrationTest
{
    private static final int CLIENT_SOCKET_TIMEOUT_MS = 5000;
    private static final int TEST_TIMEOUT_SECONDS = 15;

    /**
     * A free TCP port is selected before the Spring context starts.
     *
     * SocketServer reads server.socket.port from Spring configuration.
     */
    private static final int TEST_SERVER_PORT = findFreePort();

    @DynamicPropertySource
    static void registerTestProperties(DynamicPropertyRegistry registry)
    {
        registry.add(
                "server.socket.port",
                () -> TEST_SERVER_PORT
        );
    }

    @Autowired
    private SocketServer socketServer;

    @Autowired
    private Gson gson;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    // ========================================================================
    // Lifecycle
    // ========================================================================

    @BeforeAll
    void beforeAll()
    {
        /*
         * SocketServer is already started by Spring through @PostConstruct.
         *
         * We intentionally do NOT create a second server and do NOT call
         * socketServer.start() manually.
         */
        waitForServerToBecomeAvailable();
    }


    @AfterAll
    void afterAll()
    {
        /*
         * SocketServer is a Spring-managed bean and its @PreDestroy method
         * will shut it down when the Spring context is closed.
         *
         * Therefore we do not manually call shutdown() here.
         */
    }


    @BeforeEach
    void cleanDatabase()
    {
        jdbcTemplate.update("DELETE FROM sessions");
        jdbcTemplate.update("DELETE FROM users");
    }


    // ========================================================================
    // Tests
    // ========================================================================

    @Test
    @DisplayName("Two simultaneous logins for SAME user - Race Condition Check")
    void testConcurrentLoginSameUser() throws Exception
    {
        String username = "testUser";
        String password = "StrongPassword123";

        registerUser(username, password);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        ResponseEnvelope[] responses = new ResponseEnvelope[2];
        Throwable[] errors = new Throwable[2];

        executor.submit(() ->
        {
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

        executor.submit(() ->
        {
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

        boolean finished =
                doneLatch.await(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        executor.shutdownNow();

        assertTrue(
                finished,
                "Login tasks did not finish in time"
        );

        assertNull(
                errors[0],
                "First login task failed with exception: " + errors[0]
        );

        assertNull(
                errors[1],
                "Second login task failed with exception: " + errors[1]
        );

        assertNotNull(
                responses[0],
                "First login response should not be null"
        );

        assertNotNull(
                responses[1],
                "Second login response should not be null"
        );

        assertTrue(
                responses[0].isSuccess() || responses[1].isSuccess(),
                "At least one concurrent login should succeed"
        );

        if (!responses[0].isSuccess())
        {
            System.out.println(
                    "Login 1 failed: "
                            + responses[0].errorCode()
                            + " - "
                            + responses[0].errorMessage()
            );
        }

        if (!responses[1].isSuccess())
        {
            System.out.println(
                    "Login 2 failed: "
                            + responses[1].errorCode()
                            + " - "
                            + responses[1].errorMessage()
            );
        }

        int sessionCount =
                getActiveSessionCountForUser(username);

        assertTrue(
                sessionCount <= 2,
                "Too many sessions created: " + sessionCount
        );
    }


    @Test
    @DisplayName("Invalid login should return failure envelope, not success")
    void testInvalidLoginReturnsFailure() throws Exception
    {
        ResponseEnvelope response =
                sendLogin(
                        "existingUser",
                        "WRONG_PASSWORD"
                );

        assertFalse(
                response.isSuccess(),
                "Login with wrong password should fail"
        );

        assertNotNull(
                response.getError(),
                "Error should be present"
        );

        assertTrue(
                response.getData() == null || response.getData().isJsonNull(),
                "Payload should be null on failure"
        );
    }


    @Test
    @DisplayName("Payload structure must be raw AuthResponse")
    void testPayloadStructureIsCorrect() throws Exception
    {
        registerUser(
                "validUser",
                "StrongPassword123"
        );

        ResponseEnvelope response =
                sendLogin(
                        "validUser",
                        "StrongPassword123"
                );

        assertTrue(
                response.isSuccess(),
                "Login failed! Error Code: "
                        + response.errorCode()
                        + ", Message: "
                        + response.errorMessage()
        );

        JsonElement data = response.getData();

        assertNotNull(
                data,
                "Payload should not be null on success"
        );

        JsonObject payload =
                data.getAsJsonObject();

        assertTrue(
                payload.has("userId"),
                "Payload must contain userId field directly"
        );

        assertTrue(
                payload.has("token"),
                "Payload must contain token field directly"
        );

        assertTrue(
                payload.has("username"),
                "Payload must contain username field directly"
        );

        assertTrue(
                payload.has("expiresAt"),
                "Payload must contain expiresAt field directly"
        );

        assertTrue(
                payload.has("message"),
                "Payload must contain message field directly"
        );

        assertTrue(
                payload.has("sessionId"),
                "Payload must contain sessionId field directly"
        );

        assertFalse(
                payload.has("success"),
                "Payload must not contain Result.success"
        );

        assertFalse(
                payload.has("data"),
                "Payload must not contain nested Result.data"
        );

        assertFalse(
                payload.has("Data"),
                "Payload must not contain Data"
        );
    }


    @Test
    @DisplayName("Concurrent login should keep at most one active session")
    void concurrentLoginSameUserKeepsAtMostOneActiveSession()
            throws Exception
    {
        String username =
                "same_user_"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 8);

        String password = "StrongPassword123!";

        registerUser(
                username,
                password
        );

        int clientCount = 2;

        ExecutorService pool =
                Executors.newFixedThreadPool(clientCount);

        CountDownLatch start =
                new CountDownLatch(1);

        CountDownLatch done =
                new CountDownLatch(clientCount);

        List<Future<ResponseEnvelope>> futures =
                new ArrayList<>();

        for (int i = 0; i < clientCount; i++)
        {
            futures.add(
                    pool.submit(() ->
                    {
                        try
                        {
                            start.await();

                            return sendLogin(
                                    username,
                                    password
                            );
                        }
                        finally
                        {
                            done.countDown();
                        }
                    })
            );
        }

        start.countDown();

        assertTrue(
                done.await(
                        TEST_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS
                ),
                "Concurrent login tasks did not finish"
        );

        int successCount = 0;

        for (Future<ResponseEnvelope> future : futures)
        {
            ResponseEnvelope response =
                    future.get(
                            TEST_TIMEOUT_SECONDS,
                            TimeUnit.SECONDS
                    );

            assertNotNull(
                    response,
                    "Login response must not be null"
            );

            if (response.isSuccess())
            {
                successCount++;

                assertNotNull(
                        response.getData(),
                        "Successful login must contain data"
                );

                AuthResponse auth =
                        gson.fromJson(
                                response.getData(),
                                AuthResponse.class
                        );

                assertNotNull(
                        auth,
                        "AuthResponse must not be null"
                );

                assertNotNull(
                        auth.token(),
                        "AuthResponse token must not be null"
                );
            }
            else
            {
                System.out.println(
                        "Concurrent login failed: "
                                + response.getError()
                                + " / "
                                + response.errorMessage()
                );
            }
        }

        assertTrue(
                successCount >= 1,
                "At least one login should succeed"
        );

        int activeSessions =
                getActiveSessionCountForUser(username);

        assertTrue(
                activeSessions <= 1,
                "Expected at most 1 active session, got "
                        + activeSessions
                        + " (total="
                        + getTotalSessionCountForUser(username)
                        + ")"
        );

        pool.shutdownNow();
    }


    @Test
    @DisplayName("Concurrent Login and Register via Real Socket E2E Test")
    void concurrentClientsShouldMaintainIsolatedSessionsAndSucceed()
            throws Exception
    {
        int clientCount = 2;

        ExecutorService clientExecutor =
                Executors.newFixedThreadPool(clientCount);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        CountDownLatch finishLatch =
                new CountDownLatch(clientCount);

        AuthResponse[] clientResponses =
                new AuthResponse[clientCount];

        Throwable[] exceptions =
                new Throwable[clientCount];

        for (int i = 0; i < clientCount; i++)
        {
            final int clientIndex = i;

            clientExecutor.submit(() ->
            {
                try
                {
                    startLatch.await();

                    String uniqueId =
                            UUID.randomUUID()
                                    .toString()
                                    .substring(0, 8);

                    String username =
                            "user_" + uniqueId;

                    String email =
                            "email_"
                                    + uniqueId
                                    + "@integrationtest.com";

                    String password =
                            "StrongPassword123!";

                    try (
                            Socket socket = openClientSocket();
                            PrintWriter out =
                                    new PrintWriter(
                                            socket.getOutputStream(),
                                            true
                                    );
                            BufferedReader in =
                                    new BufferedReader(
                                            new InputStreamReader(
                                                    socket.getInputStream()
                                            )
                                    )
                    )
                    {
                        RegisterRequest registerRequest =
                                new RegisterRequest(
                                        username,
                                        email,
                                        password,
                                        "Display Name"
                                );

                        RequestEnvelope registerEnvelope =
                                new RequestEnvelope(
                                        UUID.randomUUID(),
                                        RequestType.AUTH_REGISTER,
                                        gson.toJsonTree(
                                                registerRequest
                                        ),
                                        null
                                );

                        ResponseEnvelope registerResponse =
                                sendRequestOnSocket(
                                        socket,
                                        out,
                                        in,
                                        registerEnvelope
                                );

                        assertTrue(
                                registerResponse.isSuccess(),
                                "Registration failed: "
                                        + registerResponse.getError()
                        );

                        LoginRequest loginRequest =
                                new LoginRequest(
                                        username,
                                        password
                                );

                        RequestEnvelope loginEnvelope =
                                new RequestEnvelope(
                                        UUID.randomUUID(),
                                        RequestType.AUTH_LOGIN,
                                        gson.toJsonTree(
                                                loginRequest
                                        ),
                                        null
                                );

                        ResponseEnvelope loginResponse =
                                sendRequestOnSocket(
                                        socket,
                                        out,
                                        in,
                                        loginEnvelope
                                );

                        assertTrue(
                                loginResponse.isSuccess(),
                                "Login failed: "
                                        + loginResponse.getError()
                        );

                        AuthResponse authResponse =
                                gson.fromJson(
                                        loginResponse.getData(),
                                        AuthResponse.class
                                );

                        assertNotNull(
                                authResponse,
                                "AuthResponse must not be null"
                        );

                        assertNotNull(
                                authResponse.token(),
                                "Token must not be null"
                        );

                        clientResponses[clientIndex] =
                                authResponse;
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

        boolean completed =
                finishLatch.await(
                        TEST_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS
                );

        assertTrue(
                completed,
                "Test timed out. Clients did not finish in time."
        );

        clientExecutor.shutdownNow();

        for (int i = 0; i < clientCount; i++)
        {
            assertNull(
                    exceptions[i],
                    "Client Thread "
                            + i
                            + " threw exception: "
                            + exceptions[i]
            );
        }

        assertNotNull(
                clientResponses[0],
                "Client 1 response should not be null"
        );

        assertNotNull(
                clientResponses[1],
                "Client 2 response should not be null"
        );

        assertNotNull(
                clientResponses[0].token(),
                "Token for Client 1 should not be null"
        );

        assertNotNull(
                clientResponses[1].token(),
                "Token for Client 2 should not be null"
        );

        assertNotEquals(
                clientResponses[0].token(),
                clientResponses[1].token(),
                "Clients received identical tokens!"
        );
    }


    // ========================================================================
    // Socket helpers
    // ========================================================================

    private Socket openClientSocket() throws Exception
    {
        Socket socket =
                new Socket(
                        "localhost",
                        TEST_SERVER_PORT
                );

        socket.setSoTimeout(
                CLIENT_SOCKET_TIMEOUT_MS
        );

        return socket;
    }


    private ResponseEnvelope sendRequest(
            RequestEnvelope request
    ) throws Exception
    {
        try (
                Socket socket = openClientSocket();
                PrintWriter out =
                        new PrintWriter(
                                socket.getOutputStream(),
                                true
                        );
                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(
                                        socket.getInputStream()
                                )
                        )
        )
        {
            return sendRequestOnSocket(
                    socket,
                    out,
                    in,
                    request
            );
        }
    }


    private ResponseEnvelope sendRequestOnSocket(
            Socket socket,
            PrintWriter out,
            BufferedReader in,
            RequestEnvelope request
    ) throws Exception
    {
        out.println(
                gson.toJson(request)
        );

        String raw =
                in.readLine();

        assertNotNull(
                raw,
                "Server returned null response"
        );

        ResponseEnvelope response =
                gson.fromJson(
                        raw,
                        ResponseEnvelope.class
                );

        assertNotNull(
                response,
                "Failed to parse ResponseEnvelope"
        );

        return response;
    }


    // ========================================================================
    // Authentication helpers
    // ========================================================================

    private AuthResponse registerUser(
            String username,
            String password
    ) throws Exception
    {
        String email =
                username + "@example.com";

        RegisterRequest request =
                new RegisterRequest(
                        username,
                        email,
                        password,
                        "Display Name"
                );

        RequestEnvelope envelope =
                new RequestEnvelope(
                        UUID.randomUUID(),
                        RequestType.AUTH_REGISTER,
                        gson.toJsonTree(request),
                        null
                );

        ResponseEnvelope response =
                sendRequest(envelope);

        assertTrue(
                response.isSuccess(),
                "Register failed for user="
                        + username
                        + " | errorCode="
                        + response.getError()
                        + " | errorMessage="
                        + response.errorMessage()
        );

        assertNotNull(
                response.getData(),
                "Register success payload must not be null"
        );

        AuthResponse authResponse =
                gson.fromJson(
                        response.getData(),
                        AuthResponse.class
                );

        assertNotNull(
                authResponse,
                "Failed to parse AuthResponse"
        );

        assertNotNull(
                authResponse.token(),
                "Register AuthResponse.token must not be null"
        );

        assertEquals(
                username,
                authResponse.username(),
                "Registered username mismatch"
        );

        return authResponse;
    }


    private ResponseEnvelope sendLogin(
            String username,
            String password
    ) throws Exception
    {
        LoginRequest loginRequest =
                new LoginRequest(
                        username,
                        password
                );

        RequestEnvelope request =
                new RequestEnvelope(
                        UUID.randomUUID(),
                        RequestType.AUTH_LOGIN,
                        gson.toJsonTree(loginRequest),
                        null
                );

        return sendRequest(request);
    }


    // ========================================================================
    // Database helpers
    // ========================================================================

    private int getActiveSessionCountForUser(
            String username
    )
    {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM sessions s JOIN users u ON s.user_id = u.id WHERE u.username = ? AND s.expires_at > NOW()",
                Integer.class,
                username
        );

        return count != null ? count : 0;
    }


    private int getTotalSessionCountForUser(
            String username
    )
    {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM sessions s JOIN users u ON s.user_id = u.id WHERE u.username = ?",
                Integer.class,
                username
        );

        return count != null ? count : 0;
    }


    // ========================================================================
    // Test server startup helpers
    // ========================================================================

    private void waitForServerToBecomeAvailable()
    {
        long deadline =
                System.currentTimeMillis() + 10_000;

        while (System.currentTimeMillis() < deadline)
        {
            try (Socket ignored =
                         new Socket(
                                 "localhost",
                                 TEST_SERVER_PORT
                         ))
            {
                return;
            }
            catch (Exception ignored)
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();

                    throw new IllegalStateException(
                            "Interrupted while waiting for socket server",
                            e
                    );
                }
            }
        }

        fail(
                "SocketServer did not become available on port "
                        + TEST_SERVER_PORT
        );
    }


    private static int findFreePort()
    {
        try (ServerSocket socket =
                     new ServerSocket(0))
        {
            return socket.getLocalPort();
        }
        catch (Exception e)
        {
            throw new IllegalStateException(
                    "Could not find a free TCP port",
                    e
            );
        }
    }
}