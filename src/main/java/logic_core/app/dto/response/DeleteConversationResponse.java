package logic_core.app.dto.response;

import java.util.UUID;

public record DeleteConversationResponse(
        UUID conversationId
) {}
