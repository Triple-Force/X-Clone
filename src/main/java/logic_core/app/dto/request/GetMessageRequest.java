package logic_core.app.dto.request;

import java.util.UUID;

public record GetMessageRequest(
        UUID conversationId,
        UUID messageId,
        String sessionToken
) {}