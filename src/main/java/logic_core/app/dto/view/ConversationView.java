package logic_core.app.dto.view;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ConversationView(
        UUID conversationId,
        UserCardView otherUser,
        String lastMessagePreview,
        OffsetDateTime lastMessageAt,
        int unreadCount
) {}
