package logic_core.app.dto.request;

import java.util.UUID;

public record RemoveConversationMemberRequest(
        UUID memberId,
        UUID conversationId,
        String sessionToken
) {}
