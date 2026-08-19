package logic_core.app.dto.request;

import java.util.UUID;

public record EditMessageRequest(
        UUID conversationId,
        UUID messageId,
        String text,
        String sessionToken
) {}