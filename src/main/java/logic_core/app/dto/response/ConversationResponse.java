package logic_core.app.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ConversationResponse(
        UUID conversationId,
        UserResponse otherUser,
        String lastMessagePreview,
        OffsetDateTime lastMessageAt,
        int unreadCount
) {}
