package Client.transport;

import Client.config.ServerConfig;
import Client.session.ClientSession;
import com.google.gson.Gson;
import logic_core.infrastructure.transport.RequestEnvelope;
import logic_core.infrastructure.transport.ResponseEnvelope;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public final class SocketClient implements AutoCloseable
{
    private final ServerConfig config;
    private final ClientSession session;
    private final Gson gson;
    private final ReentrantLock lock = new ReentrantLock(true);

    private Socket socket;
    private BufferedWriter out;
    private BufferedReader in;

    public SocketClient(ServerConfig config, ClientSession session)
    {
        this.config = Objects.requireNonNull(config, "config");
        this.session = Objects.requireNonNull(session, "session");
        this.gson = new Gson().newBuilder().serializeNulls().create();
    }

    public void connect()
    {
        lock.lock();
        try
        {
            if (isConnectionUsable())
            {
                return;
            }

            closeInternal();

            socket = new Socket(config.host(), config.port());
            socket.setSoTimeout(config.timeoutMs());
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        }
        catch (IOException ex)
        {
            closeInternal();
            throw new UncheckedIOException(
                    "Failed to connect to server " + config.host() + ":" + config.port(),
                    ex
            );
        }
        finally
        {
            lock.unlock();
        }
    }

    public ResponseEnvelope send(RequestEnvelope request)
    {
        Objects.requireNonNull(request, "request");

        lock.lock();
        try
        {
            ensureConnected();

            RequestEnvelope effectiveRequest = attachSessionTokenIfNeeded(request);
            String payload = gson.toJson(effectiveRequest);

            return sendOnce(payload);
        }
        finally
        {
            lock.unlock();
        }
    }

    private ResponseEnvelope sendOnce(String payload)
    {
        try
        {
            out.write(payload);
            out.newLine();
            out.flush();

            String rawResponse = in.readLine();
            if (rawResponse == null || rawResponse.isBlank())
            {
                throw new IllegalStateException("Server closed the connection without returning a response.");
            }

            ResponseEnvelope response = gson.fromJson(rawResponse, ResponseEnvelope.class);
            if (response == null)
            {
                throw new IllegalStateException("Unable to parse server response.");
            }

            return response;
        }
        catch (SocketTimeoutException ex)
        {
            closeInternal();
            throw new IllegalStateException("Request timed out.", ex);
        }
        catch (SocketException ex)
        {
            closeInternal();

            throw new IllegalStateException("Socket error while sending request.", ex);
        }
        catch (IOException ex)
        {
            closeInternal();

            throw new UncheckedIOException("I/O error while sending request.", ex);
        }
    }

    private RequestEnvelope attachSessionTokenIfNeeded(
            RequestEnvelope request)
    {
        if (request.hasToken())
        {
            return request;
        }

        ClientSession.SessionSnapshot snapshot = session.snapshot();
        if (snapshot.token() == null || snapshot.token().isBlank())
        {
            return request;
        }

        return request.withToken(snapshot.token());
    }

    private void ensureConnected()
    {
        if (!isConnectionUsable())
        {
            connect();
        }
    }

    private boolean isConnectionUsable()
    {
        return socket != null
                && !socket.isClosed()
                && socket.isConnected()
                && !socket.isInputShutdown()
                && !socket.isOutputShutdown()
                && in != null
                && out != null;
    }

    private void closeInternal()
    {
        closeQuietly(in);
        closeQuietly(out);
        closeQuietly(socket);

        in = null;
        out = null;
        socket = null;
    }

    private void closeQuietly(Closeable closeable)
    {
        if (closeable == null)
        {
            return;
        }

        try
        {
            closeable.close();
        }
        catch (IOException ignored)
        {
        }
    }

    private void closeQuietly(Socket socket)
    {
        if (socket == null)
        {
            return;
        }

        try
        {
            socket.close();
        }
        catch (IOException ignored)
        {
        }
    }

    @Override
    public void close()
    {
        lock.lock();
        try
        {
            closeInternal();
        }
        finally
        {
            lock.unlock();
        }
    }
}
