package logic_core.app.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ConversationStateResponse(
        UUID conversationId,
        String lastMessagePreview,
        OffsetDateTime lastMessageAt,
        int unreadCount,
        ConversationInfoResponse infoResponse
) {}