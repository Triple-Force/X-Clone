package logic_core.app.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UserResponse sender,
        String text,
        OffsetDateTime sentAt
) {}
