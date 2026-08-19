package logic_core.infrastructure.transport.server;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketServer
{
    private final int port;
    private final RequestDispatcher dispatcher;
    private final Gson gson;
    private final ExecutorService clientPool;
    private ServerSocket serverSocket;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public SocketServer(int port, RequestDispatcher dispatcher, Gson gson)
    {
        if (port <= 0 || port > 65535)
        {
            throw new IllegalArgumentException("Port must be between 1 and 65535.");
        }
        this.port = port;
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher must not be null");
        this.gson = Objects.requireNonNull(gson, "gson must not be null");
        this.clientPool = Executors.newFixedThreadPool(100);
    }

    public void start()
    {
        if (!isRunning.compareAndSet(false, true))
        {
            return;
        }
        try
        {
            this.serverSocket = new ServerSocket(port);

            while (isRunning.get())
            {
                try
                {
                    Socket socket = serverSocket.accept();
                    clientPool.submit(new ClientHandler(socket, dispatcher, gson));
                }
                catch (IOException e)
                {
                    if (!isRunning.get())
                    {
                        break;
                    }
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to start socket server on port " + port, e);
        }
        finally
        {
            shutdown();
        }
    }

    public void stop()
    {
        shutdown();
    }

    public void shutdown()
    {
        if (!isRunning.compareAndSet(true, false))
        {
            return;
        }
        try
        {
            if (serverSocket != null && !serverSocket.isClosed())
            {
                serverSocket.close();
            }
        }
        catch (IOException e)
        {
            System.err.println("Error closing server socket: " + e.getMessage());
        }

        clientPool.shutdown();
        try
        {
            if (!clientPool.awaitTermination(5, TimeUnit.SECONDS))
            {
                clientPool.shutdownNow();
            }
        }
        catch (InterruptedException e)
        {
            clientPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
