package Testing;

import Client.ClientApplicationContext;
import Client.config.ServerConfig;
import Testing.helper.DatabaseHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.persistence.EntityManagerFactory;
import logic_core.app.DependencyContainer;
import logic_core.infrastructure.transport.server.RequestDispatcher;
import logic_core.infrastructure.transport.server.SocketServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class XCloneTest2
{
    // =========================================================
    // Constants
    // =========================================================

    protected static final String HOST = "127.0.0.1";

    protected static final String PU = "X-Clone-PU";

    protected static final int CLIENT_TIMEOUT = 5000;

    protected static final int SERVER_STARTUP_TIMEOUT = 5000;

    // =========================================================
    // Infrastructure
    // =========================================================

    protected final Gson gson =
            new GsonBuilder().create();

    protected EntityManagerFactory emf;

    protected SocketServer socketServer;

    protected ExecutorService serverExecutor;

    protected int serverPort;

    // =========================================================
    // Default client
    // =========================================================

    protected ClientApplicationContext client;

    // =========================================================
    // Lifecycle
    // =========================================================

    protected DatabaseHelper db;

    @BeforeAll
    void beforeAll() throws Exception
    {
        emf = DependencyContainer.entityManagerFactory();
        db = new DatabaseHelper(emf);

        serverPort = findFreePort();

        RequestDispatcher dispatcher =
                new RequestDispatcher(gson);
        socketServer = new SocketServer(
                        serverPort,
                        dispatcher,
                        gson
        );

        serverExecutor = Executors.newSingleThreadExecutor(r ->
                {
                    Thread t = new Thread(r);
                    t.setDaemon(true);
                    t.setName("SocketServer");
                    return t;
                });

        serverExecutor.submit(socketServer::start);

        waitForServer();

        rebuildClient();
    }

    @BeforeEach
    void beforeEach()
    {
        db.clearDatabase();

        rebuildClient();
    }

    @AfterAll
    void afterAll()
    {
        closeClient();

        if (socketServer != null)
        {
            socketServer.stop();
        }

        if (serverExecutor != null)
        {
            serverExecutor.shutdownNow();

            try
            {
                serverExecutor.awaitTermination(
                        5,
                        TimeUnit.SECONDS
                );
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }

        if (emf != null)
        {
            emf.close();
        }
    }

    // =========================================================
    // Client
    // =========================================================

    protected void rebuildClient()
    {
        closeClient();

        ServerConfig config =
                new ServerConfig(
                        HOST,
                        serverPort,
                        CLIENT_TIMEOUT
                );

        client = new ClientApplicationContext(config);
    }

    protected void closeClient()
    {
        if (client == null)
        {
            return;
        }

        try
        {
            client.close();
        }
        catch (Exception ignored)
        {
        }

        client = null;
    }

    // =========================================================
    // Helpers
    // =========================================================

    protected int findFreePort() throws IOException
    {
        try (ServerSocket socket = new ServerSocket(0))
        {
            return socket.getLocalPort();
        }
    }

    protected void waitForServer() throws Exception
    {
        long deadline =
                System.currentTimeMillis()
                        + SERVER_STARTUP_TIMEOUT;

        while (System.currentTimeMillis() < deadline)
        {
            try (Socket ignored = new Socket(HOST, serverPort))
            {
                return;
            }
            catch (IOException ignored)
            {
                Thread.sleep(100);
            }
        }

        throw new IllegalStateException(
                "SocketServer did not start."
        );
    }
}