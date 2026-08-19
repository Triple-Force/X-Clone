package Testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SocketAbruptDisconnectTest extends XCloneTest
{
    @Test
    @DisplayName("server survives abrupt client disconnects and stays healthy")
    void abruptDisconnects() throws Exception
    {
        assertServerHealthy();

        int clients = 32;
        ExecutorService pool = Executors.newFixedThreadPool(clients);
        CountDownLatch ready = new CountDownLatch(clients);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(clients);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        try
        {
            for (int i = 0; i < clients; i++)
            {
                pool.submit(() -> {
                    ready.countDown();
                    try
                    {
                        if (!startGate.await(5, TimeUnit.SECONDS))
                        {
                            throw new AssertionError("start gate was not released in time");
                        }

                        try (Socket socket = new Socket())
                        {
                            socket.connect(new InetSocketAddress(HOST, serverPort), 2_000);
                            socket.setSoLinger(true, 0); // abortive close: forces abrupt disconnect
                        }
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

            assertTrue(ready.await(5, TimeUnit.SECONDS), "clients did not become ready in time");
            startGate.countDown();

            assertTrue(done.await(30, TimeUnit.SECONDS), "client disconnect storm did not finish in time");
        }
        finally
        {
            pool.shutdownNow();
        }

        assertTrue(errors.isEmpty(), () -> "unexpected client-side failures: " + errors);
        assertServerHealthy();
    }

    protected void assertServerHealthy() throws Exception
    {
        try (Socket socket = new Socket())
        {
            socket.connect(new InetSocketAddress(HOST, serverPort), 2_000);
            socket.setSoLinger(true, 0);
        }
    }
}
