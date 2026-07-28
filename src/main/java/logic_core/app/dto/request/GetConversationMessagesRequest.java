package logic_core.app.dto.request;

import java.util.UUID;

public record GetConversationMessagesRequest(
        UUID conversationId,
        int page,
        int pageSize,
        String sessionToken
) {}