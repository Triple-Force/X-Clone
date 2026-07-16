package logic_core.infrastructure.transport;

import com.google.gson.JsonElement;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;


@Builder
public record ResponseEnvelope(

        UUID requestId,
        boolean success,
        String type,
        JsonElement payload,
        String errorCode,
        String errorMessage,
        OffsetDateTime timestamp
)
{
    public ResponseEnvelope
    {
        if (requestId == null)
        {
            requestId = UUID.randomUUID();
        }

        if (type == null || type.isBlank())
        {
            throw new IllegalArgumentException(
                    "Response type cannot be null or blank."
            );
        }

        if (timestamp == null)
        {
            timestamp = OffsetDateTime.now();
        }
    }

    public static ResponseEnvelope success(
            UUID requestId,
            String type,
            JsonElement payload
    )
    {
        return new ResponseEnvelope(
                requestId,
                true,
                type,
                payload,
                null,
                null,
                OffsetDateTime.now()
        );
    }

    public static ResponseEnvelope failure(
            UUID requestId,
            String type,
            String errorCode,
            String errorMessage
    )
    {
        return new ResponseEnvelope(
                requestId,
                false,
                type,
                null,
                errorCode,
                errorMessage,
                OffsetDateTime.now()
        );
    }


    public boolean isSuccess()
    {
        return success;
    }


    public String getError()
    {
        return errorCode;
    }


    public JsonElement getData()
    {
        return payload;
    }
}
