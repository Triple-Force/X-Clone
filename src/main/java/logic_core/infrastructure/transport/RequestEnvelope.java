package logic_core.infrastructure.transport;

import com.google.gson.JsonElement;
import java.util.UUID;

public record RequestEnvelope(
        UUID requestId,
        RequestType type,
        JsonElement payload,
        String token
)
{
    public RequestEnvelope
    {
        if (requestId == null)
        {
            requestId = UUID.randomUUID();
        }
        if (type == null)
        {
            throw new IllegalArgumentException(
                    "Request type must not be null or blank"
            );
        }
    }

    public RequestEnvelope withToken(String newToken)
    {
        return new RequestEnvelope(requestId, type, payload, newToken);
    }

    public boolean hasToken()
    {
        return token != null && !token.isBlank();
    }
}

