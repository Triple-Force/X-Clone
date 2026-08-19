package logic_core.app.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ConversationInfoResponse(
        UUID conversationId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<UUID>participantIds
) {}