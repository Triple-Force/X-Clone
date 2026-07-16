package Testing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import logic_core.app.dto.request.LoginRequest;
import logic_core.app.dto.request.RegisterRequest;
import logic_core.app.dto.response.AuthResponse;
import logic_core.infrastructure.transport.RequestEnvelope;
import logic_core.infrastructure.transport.RequestType;
import logic_core.infrastructure.transport.ResponseEnvelope;
import logic_core.infrastructure.transport.server.RequestDispatcher;
import logic_core.infrastructure.transport.server.SocketServer;
import org.junit.jupiter.api.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RobustConcurrentAuthIntegrationTest
{
    private EntityManagerFactory emf;
    private int serverPort;
    private SocketServer socketServer;
    private final Gson gson = new GsonBuilder().create();
    private ExecutorService serverExecutor;

    @BeforeAll
    void setupServer() throws Exception
    {
        emf = Persistence.createEntityManagerFactory("X-Clone-PU");
        serverPort = findFreePort();

        RequestDispatcher dispatcher = new RequestDispatcher(gson);
        socketServer = new SocketServer(serverPort, dispatcher, gson);

        serverExecutor = Executors.newSingleThreadExecutor();
        serverExecutor.submit(socketServer::start);

        waitForServer(serverPort);
    }

    @BeforeEach
    void cleanDatabase()
    {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Session").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    @Test
    @DisplayName("Stress Test: Concurrent Registration of Unique Users")
    void testConcurrentRegistration() throws Exception
    {
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        List<ResponseEnvelope> responses = Collections.synchronizedList(new ArrayList<>());
        Set<String> observedTokens = Collections.synchronizedSet(new HashSet<>());

        for (int i = 0; i < threadCount; i++)
        {
            final int id = i;
            executor.submit(() -> {
                try
                {
                    barrier.await(); // Sync all threads to start at once

                    try (Socket socket = new Socket("localhost", serverPort);
                         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())))
                    {
                        String username = "user_" + id + "_" + UUID.randomUUID().toString().substring(0, 5);
                        RegisterRequest regReq = new RegisterRequest(
                                username,
                                username + "@example.com",
                                "StrongPassword123!",
                                "Display"
                        );

                        UUID requestId = UUID.randomUUID();
                        RequestEnvelope env = new RequestEnvelope(requestId, RequestType.AUTH_REGISTER, gson.toJsonTree(regReq), null);

                        out.println(gson.toJson(env));
                        String rawResponse = in.readLine();
                        ResponseEnvelope resp = gson.fromJson(rawResponse, ResponseEnvelope.class);

                        responses.add(resp);

                        if (resp.isSuccess())
                        {
                            AuthResponse auth = gson.fromJson(resp.payload(), AuthResponse.class);
                            observedTokens.add(auth.token());
                            assertEquals(requestId, resp.requestId(), "Request ID mismatch");
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    finishLatch.countDown();
                }
            });
        }

        assertTrue(finishLatch.await(30, TimeUnit.SECONDS), "Test timed out");

        // Assertions
        long successCount = responses.stream().filter(ResponseEnvelope::isSuccess).count();
        assertEquals(threadCount, successCount, "Not all registrations were successful");
        assertEquals(threadCount, observedTokens.size(), "Tokens are not unique across concurrent requests");

        // DB State Verification
        EntityManager em = emf.createEntityManager();
        long userCount = (long) em.createQuery("SELECT COUNT(u) FROM User u").getSingleResult();
        long sessionCount = (long) em.createQuery("SELECT COUNT(s) FROM Session s").getSingleResult();
        em.close();

        assertEquals(threadCount, userCount, "Database user count mismatch");
        assertEquals(threadCount, sessionCount, "Database session count mismatch");
    }

    @Test
    @DisplayName("Race Condition Test: Concurrent Login for Same User")
    void testConcurrentLoginSameUser() throws Exception
    {
        // 1. Register a user first
        String username = "race_user";
        registerUser(username, "Password123!");

        int loginAttempts = 8;
        ExecutorService executor = Executors.newFixedThreadPool(loginAttempts);
        CyclicBarrier barrier = new CyclicBarrier(loginAttempts);
        CountDownLatch latch = new CountDownLatch(loginAttempts);
        List<AuthResponse> successfulLogins = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < loginAttempts; i++)
        {
            executor.submit(() -> {
                try
                {
                    barrier.await();
                    try (Socket socket = new Socket("localhost", serverPort);
                         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())))
                    {

                        LoginRequest loginReq = new LoginRequest(username, "Password123!");
                        RequestEnvelope env = new RequestEnvelope(UUID.randomUUID(), RequestType.AUTH_LOGIN, gson.toJsonTree(loginReq), null);

                        out.println(gson.toJson(env));
                        ResponseEnvelope resp = gson.fromJson(in.readLine(), ResponseEnvelope.class);

                        if (resp.isSuccess())
                        {
                            successfulLogins.add(gson.fromJson(resp.payload(), AuthResponse.class));
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    latch.countDown();
                }
            });
        }

        latch.await(20, TimeUnit.SECONDS);

        // Assert based on SessionDao.replaceUserSession logic (Single Active Session)
        assertFalse(successfulLogins.isEmpty(), "At least one login should succeed");

        EntityManager em = emf.createEntityManager();
        // The policy is replaceUserSession: should only have 1 session in DB finally
        long activeSessions = (long) em.createQuery("SELECT COUNT(s) FROM Session s WHERE s.user.username = :un")
                .setParameter("un", username)
                .getSingleResult();
        em.close();

        assertEquals(1, activeSessions, "Concurrency race in replaceUserSession: multiple sessions survived");
    }

    @AfterAll
    void tearDown()
    {
        if (socketServer != null) socketServer.stop();
        if (serverExecutor != null) serverExecutor.shutdownNow();
        if (emf != null) emf.close();
    }

    // Helper Methods
    private int findFreePort() throws IOException
    {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0))
        {
            return socket.getLocalPort();
        }
    }

    private void waitForServer(int port)
    {
        int retries = 5;
        while (retries > 0)
        {
            try (Socket s = new Socket("localhost", port))
            {
                return;
            }
            catch (IOException e)
            {
                retries--;
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
        }
        throw new RuntimeException("Server failed to start");
    }

    private void registerUser(String username, String password) throws IOException
    {
        try (Socket socket = new Socket("localhost", serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())))
        {
            RegisterRequest req = new RegisterRequest(username ,username+"@test.com", password, "Test");
            RequestEnvelope env = new RequestEnvelope(UUID.randomUUID(), RequestType.AUTH_REGISTER, gson.toJsonTree(req), null);
            out.println(gson.toJson(env));
            in.readLine();
        }
    }
}
