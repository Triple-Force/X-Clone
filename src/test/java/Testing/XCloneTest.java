package Testing;

import Client.ClientApplicationContext;
import Client.Service.AuthClientService;
import Client.Service.AuthClientService.AuthResult;
import Client.config.ServerConfig;
import Client.session.ClientSession;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import logic_core.app.dto.response.AuthResponse;
import logic_core.infrastructure.transport.server.RequestDispatcher;
import logic_core.infrastructure.transport.server.SocketServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base for end-to-end auth integration tests.
 * Mirrors existing concurrent integration tests:
 * - dynamic free port
 * - SocketServer in a dedicated thread
 * - X-Clone-PU EntityManagerFactory for DB verification
 * - real ClientApplicationContext + AuthClientService path
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class XCloneTest
{
    protected static final String PU = "X-Clone-PU";
    protected static final String HOST = "127.0.0.1";
    protected static final int CLIENT_TIMEOUT_MS = 5_000;

    protected final Gson gson = new GsonBuilder().create();

    protected EntityManagerFactory emf;
    protected int serverPort;
    protected SocketServer socketServer;
    protected ExecutorService serverExecutor;

    /** Shared client context for sequential E2E flows. */
    protected ClientApplicationContext clientContext;
    protected AuthClientService authService;

    @BeforeAll
    void setUpServerAndDb() throws Exception
    {
        emf = Persistence.createEntityManagerFactory(PU);

        serverPort = findFreePort();
        RequestDispatcher dispatcher = new RequestDispatcher(gson);
        socketServer = new SocketServer(serverPort, dispatcher, gson);

        serverExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "e2e-socket-server");
            t.setDaemon(true);
            return t;
        });
        serverExecutor.submit(socketServer::start);
        waitForServer(serverPort, 5_000);

        rebuildClient();
    }

    @AfterAll
    void tearDownAll()
    {
        closeClientQuietly();

        if (socketServer != null)
        {
            socketServer.stop();
        }
        if (serverExecutor != null)
        {
            serverExecutor.shutdownNow();
            try
            {
                serverExecutor.awaitTermination(5, TimeUnit.SECONDS);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
        if (emf != null && emf.isOpen())
        {
            emf.close();
        }
    }

    @BeforeEach
    void cleanDatabaseAndClient()
    {
        clearAuthTables();
        // Fresh client session/socket state between tests
        rebuildClient();
    }

    // ------------------------------------------------------------------
    // Client helpers (true client-stack E2E)
    // ------------------------------------------------------------------

    protected void rebuildClient()
    {
        closeClientQuietly();
        ServerConfig config = new ServerConfig(HOST, serverPort, CLIENT_TIMEOUT_MS);
        clientContext = new ClientApplicationContext(config);
        authService = new AuthClientService(clientContext);
    }

    protected void closeClientQuietly()
    {
        if (clientContext != null)
        {
            try
            {
                clientContext.close();
            }
            catch (Exception ignored)
            {

            }
            clientContext = null;
            authService = null;
        }
    }

    /**
     * Independent client stack — use for concurrent multi-client scenarios.
     * Caller must close the returned context.
     */
    protected ClientApplicationContext newClientContext()
    {
        return new ClientApplicationContext(
                new ServerConfig(HOST, serverPort, CLIENT_TIMEOUT_MS)
        );
    }

    protected AuthResult<AuthResponse> register(
            String username,
            String email,
            String password,
            String displayName
    ) throws Exception
    {
        return authService
                .register(username, email, password, displayName)
                .get(10, TimeUnit.SECONDS);
    }

    protected AuthResult<AuthResponse> login(String username, String password)
            throws Exception
    {
        return authService
                .login(username, password)
                .get(10, TimeUnit.SECONDS);
    }

    protected AuthResult<AuthResponse> registerAndLoginHappyPath(
            String username,
            String password
    ) throws Exception
    {
        String email = username + "@e2e.test";
        AuthResult<AuthResponse> reg =
                register(username, email, password, "E2E User");
        assertTrue(reg.isSuccess(), () -> "register failed: " + reg.errorCode()
                + " / " + reg.errorMessage());
        assertSessionMatches(reg.data(), clientContext.session());

        // New client simulating "app restart" then login
        rebuildClient();
        AuthResult<AuthResponse> loginResult = login(username, password);
        assertTrue(loginResult.isSuccess(),
                () -> "login failed: " + loginResult.errorCode()
                        + " / " + loginResult.errorMessage());
        return loginResult;
    }

    protected void assertSessionMatches(AuthResponse auth, ClientSession session)
    {
        assertNotNull(auth);
        assertNotNull(auth.token());
        assertNotNull(auth.userId());
        assertTrue(session.isLoggedIn());
        assertEquals(auth.token(), session.getToken());
        assertEquals(auth.userId(), session.getCurrentUserId());
    }

    // ------------------------------------------------------------------
    // DB helpers (same style as existing concurrent tests)
    // ------------------------------------------------------------------

    protected void clearAuthTables()
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Session").executeUpdate();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.getTransaction().commit();
        }
        catch (RuntimeException ex)
        {
            if (em.getTransaction().isActive())
            {
                em.getTransaction().rollback();
            }
            throw ex;
        }
        finally
        {
            em.close();
        }
    }

    protected long countUsers()
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            return em.createQuery("SELECT COUNT(u) FROM User u", Long.class)
                    .getSingleResult();
        }
        finally
        {
            em.close();
        }
    }

    protected long countSessionsForUsername(String username)
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            return em.createQuery(
                            "SELECT COUNT(s) FROM Session s WHERE s.user.username = :un",
                            Long.class
                    )
                    .setParameter("un", username)
                    .getSingleResult();
        }
        finally
        {
            em.close();
        }
    }

    protected boolean userExists(String username)
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            Long n = em.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.username = :un",
                            Long.class
                    )
                    .setParameter("un", username)
                    .getSingleResult();
            return n != null && n > 0;
        }
        finally
        {
            em.close();
        }
    }

    // ------------------------------------------------------------------
    // Infra helpers
    // ------------------------------------------------------------------

    protected int findFreePort() throws IOException
    {
        try (ServerSocket socket = new ServerSocket(0))
        {
            return socket.getLocalPort();
        }
    }

    protected void waitForServer(int port, long timeoutMs) throws Exception
    {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline)
        {
            try (Socket s = new Socket(HOST, port))
            {
                return;
            }
            catch (IOException ignored)
            {
                Thread.sleep(100);
            }
        }
        throw new IllegalStateException(
                "Server did not accept connections on port " + port
                        + " within " + timeoutMs + "ms"
        );
    }

    protected String uniqueUsername(String prefix)
    {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    protected void assertServerHealthy() throws Exception
    {
        try (ClientApplicationContext ctx = newClientContext())
        {
            AuthClientService svc = new AuthClientService(ctx);
            AuthResult<AuthResponse> res =
                    svc.login("___health_check_user___", "InvalidPass123!")
                            .get(5, TimeUnit.SECONDS);

            assertNotNull(res);
            assertFalse(res.isSuccess());
            assertNotNull(res.errorCode());
        }
    }
}