package logic_core.app.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String type,
        UserResponse actor,
        String message,
        boolean read,
        OffsetDateTime createdAt
) {}