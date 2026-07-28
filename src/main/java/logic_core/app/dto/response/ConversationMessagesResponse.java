package logic_core.app.dto.response;

import java.util.List;
import java.util.UUID;

public record ConversationMessagesResponse(
        UUID conversationId,
        List<MessageInfoResponse> messages,
        int limit,
        int offset,
        boolean hasMore
) {}