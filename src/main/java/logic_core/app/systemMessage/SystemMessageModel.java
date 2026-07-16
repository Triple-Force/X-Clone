package logic_core.app.systemMessage;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record SystemMessageModel(
        UUID id,
        UUID recipientId,
        SystemMessageType type,
        String title,
        String body,
        SystemMessagePriority priority,
        OffsetDateTime createdAt,
        Map<String, String> metadata
)
{}
