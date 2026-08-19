package logic_core.app.dto.request;

import java.util.UUID;

public record SendMessageRequest(
        UUID conversationId,
        String text,
        String sessionToken
) {}