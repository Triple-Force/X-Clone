package logic_core.app.dto.response;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record ConversationSummaryResponse(
        UUID conversationId,
        String title,
        String lastMessage,
        OffsetDateTime lastMessageAt,
        int unreadCount
) {}