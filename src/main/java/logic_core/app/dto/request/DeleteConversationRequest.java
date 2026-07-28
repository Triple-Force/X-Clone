package logic_core.app.dto.request;

import java.util.UUID;

public record DeleteConversationRequest(
   UUID conversationId,
   UUID deleterId,
   String sessionToken
) {}
