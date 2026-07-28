package logic_core.infrastructure.transport.server;

import com.google.gson.Gson;
import logic_core.app.dto.response.AuthResponse;
import logic_core.app.security.AuthContext;
import logic_core.common.security.AuthPrincipal;
import logic_core.infrastructure.transport.RequestEnvelope;
import logic_core.infrastructure.transport.ResponseEnvelope;
import logic_core.infrastructure.transport.ResponseType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

public class ClientHandler implements Runnable
{
    private final Socket socket;
    private final RequestDispatcher dispatcher;
    private final Gson gson;

    private volatile AuthPrincipal authenticatedUser;

    public ClientHandler(Socket socket, RequestDispatcher dispatcher, Gson gson)
    {
        this.socket = Objects.requireNonNull(socket, "socket must not be null");
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher must not be null");
        this.gson = Objects.requireNonNull(gson, "gson must not be null");
    }

    @Override
    public void run()
    {
        try
        {
            socket.setSoTimeout(30000);
        }
        catch (IOException e)
        {
            System.err.println("Failed to set socket timeout: " + e.getMessage());
            return;
        }

        try (Socket ignored = socket;
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)))
        {

            String line;
            while ((line = reader.readLine()) != null)
            {
                ResponseEnvelope response = handleRawMessage(line);
                writer.write(gson.toJson(response));
                writer.newLine();
                writer.flush();
            }
        }
        catch (SocketTimeoutException e)
        {
            System.out.println("Client socket read timed out: " + socket.getRemoteSocketAddress());
        }
        catch (IOException e)
        {
            throw new UncheckedIOException("I/O error while handling client socket", e);
        }
    }

    private ResponseEnvelope handleRawMessage(String rawMessage)
    {
        try
        {
            RequestEnvelope request = gson.fromJson(rawMessage, RequestEnvelope.class);
            return handleRequest(request);
        }
        catch (Exception e)
        {
            return ResponseEnvelope.failure(
                    null,
                    "BAD_REQUEST",
                    "MALFORMED_JSON",
                    "Invalid payload format: " + e.getMessage()
            );
        }
    }

    private ResponseEnvelope handleRequest(RequestEnvelope request)
    {
        try
        {
            getAuthenticatedUser().ifPresent(AuthContext::set);
            ResponseEnvelope response = dispatcher.dispatch(request);

            if (response.success())
            {
                updateSessionState(response);
            }

            return response;
        }
        finally
        {
            AuthContext.clear();
        }
    }

    private void updateSessionState(ResponseEnvelope response)
    {
        String responseType = response.type();

        if (ResponseType.AUTH_LOGIN_RESPONSE.toWire().equals(responseType) ||
                ResponseType.AUTH_REFRESH_RESPONSE.toWire().equals(responseType))
        {
            AuthResponse authResponse = gson.fromJson(response.payload(), AuthResponse.class);
            this.authenticatedUser = new AuthPrincipal(
                    authResponse.userId(),
                    authResponse.username(),
                    authResponse.sessionId(),
                    authResponse.token()
            );

        }
        else if (ResponseType.AUTH_LOGOUT_RESPONSE.toWire().equals(responseType))
        {
            this.authenticatedUser = null;
        }
    }

    public Optional<AuthPrincipal> getAuthenticatedUser()
    {
        return Optional.ofNullable(authenticatedUser);
    }
}
