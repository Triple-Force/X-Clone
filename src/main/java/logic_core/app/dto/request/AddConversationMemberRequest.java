package logic_core.app.dto.request;

import java.util.UUID;

public record AddConversationMemberRequest(
        UUID conversationId,
        UUID memberId,
        String sessionToken
) {}
