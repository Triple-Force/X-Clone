package logic_core.app.dto.request;

import java.util.UUID;

public record GetIsFollowingRequest(
        UUID userId,
        UUID tagetId
){}
