package logic_core.app.dto.request;

import java.util.UUID;

public record DeleteMessageRequest(
        UUID conversationId,
        UUID messageId,
        String sessionToken
) {}