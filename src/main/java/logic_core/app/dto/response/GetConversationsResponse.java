package logic_core.app.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record GetConversationsResponse(
        List<ConversationSummaryResponse> conversations,
        long totalItems,
        int page,
        int pageSize,
        boolean hasNext
) {}