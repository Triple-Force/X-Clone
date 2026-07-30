package logic_core.app.dto.request;

import java.util.UUID;

public record UpdateEmailRequest(
        String sessionToken,
        UUID userId,
        String email
)
{
}