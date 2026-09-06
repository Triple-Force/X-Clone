package logic_core.infrastructure.transport.server;

import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class SocketServer
{
    private final int port;
    private final RequestDispatcher dispatcher;
    private final Gson gson;

    private final ExecutorService clientPool;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private volatile ServerSocket serverSocket;

    public SocketServer(
            @Value("${server.socket.port:9090}") int port,
            RequestDispatcher dispatcher,
            Gson gson
    )
    {
        if (port <= 0 || port > 65535)
        {
            throw new IllegalArgumentException(
                    "Socket server port must be between 1 and 65535."
            );
        }

        this.port = port;
        this.dispatcher = dispatcher;
        this.gson = gson;
        this.clientPool = Executors.newFixedThreadPool(100);
    }

    @PostConstruct
    public void start()
    {
        if (!running.compareAndSet(false, true))
        {
            return;
        }

        Thread serverThread = new Thread(
                this::runServer,
                "socket-server-" + port
        );

        serverThread.setDaemon(false);
        serverThread.start();
    }

    private void runServer()
    {
        try
        {
            serverSocket = new ServerSocket(port);

            while (running.get())
            {
                try
                {
                    Socket socket = serverSocket.accept();

                    clientPool.submit(
                            new ClientHandler(socket, dispatcher, gson)
                    );
                }
                catch (IOException e)
                {
                    if (running.get())
                    {
                        System.err.println(
                                "Error accepting socket connection: "
                                        + e.getMessage()
                        );
                    }
                }
            }
        }
        catch (IOException e)
        {
            if (running.get())
            {
                throw new IllegalStateException(
                        "Failed to start socket server on port " + port,
                        e
                );
            }
        }
        finally
        {
            closeServerSocket();
        }
    }

    @PreDestroy
    public void shutdown()
    {
        if (!running.compareAndSet(true, false))
        {
            return;
        }

        closeServerSocket();

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

    private void closeServerSocket()
    {
        ServerSocket socket = serverSocket;

        if (socket == null || socket.isClosed())
        {
            return;
        }

        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            System.err.println(
                    "Error closing socket server: "
                            + e.getMessage()
            );
        }
    }
}