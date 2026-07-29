package logic_core.app.dto.request;

import lombok.Builder;

@Builder
public record GetConversationsRequest(
        int page,
        int pageSize,
        String sessionToken
) {}