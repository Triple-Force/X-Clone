package logic_core.infrastructure.transport.http;

import com.google.gson.Gson;
import logic_core.infrastructure.transport.RequestEnvelope;
import logic_core.infrastructure.transport.ResponseEnvelope;
import logic_core.infrastructure.transport.server.RequestDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal HTTP transport for the existing socket protocol.
 *
 * <p>POST /api accepts the exact same JSON {@link RequestEnvelope} that the socket
 * transport sends and returns the {@link ResponseEnvelope} produced by the existing
 * {@link RequestDispatcher}, serialized with the same {@link Gson} bean the socket
 * transport uses. Malformed input produces the identical failure envelope the socket
 * {@code ClientHandler} produces, so no second error-handling system is introduced.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HttpTransportController
{
    private final RequestDispatcher dispatcher;
    private final Gson gson;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String dispatch(@RequestBody String rawBody)
    {
        try
        {
            RequestEnvelope request = gson.fromJson(rawBody, RequestEnvelope.class);
            ResponseEnvelope response = dispatcher.dispatch(request);
            return gson.toJson(response);
        }
        catch (Exception e)
        {
            return gson.toJson(ResponseEnvelope.failure(
                    null,
                    "BAD_REQUEST",
                    "MALFORMED_JSON",
                    "Invalid payload format: " + e.getMessage()
            ));
        }
    }
}