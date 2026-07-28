package logic_core.app.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageInfoResponse(
        UUID messageId,
        UUID conversationId,
        UUID senderId,
        String content,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        boolean isRead,
        boolean edited
) {}