package logic_core.app.dto.request;

import java.util.UUID;

public record UpdatePasswordRequest(
        String sessionToken,
        UUID userId,
        String oldPassword,
        String newPassword
)
{
}