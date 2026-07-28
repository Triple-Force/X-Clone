package logic_core.app.dto.request;

import java.util.List;
import java.util.UUID;

public record CreateConversationRequest(
        UUID creatorId,
        List<UUID> participantIds,
        String sessionToken
) {}
