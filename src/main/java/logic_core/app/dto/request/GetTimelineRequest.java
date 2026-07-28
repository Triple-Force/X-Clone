package logic_core.app.dto.request;

import logic_core.domain.repository.TimelineType;

import java.util.UUID;

public record GetTimelineRequest(
        TimelineType timelineType,
        UUID actorId,
        UUID targetUserId,
        int page,
        int pageSize,
        String sessionToken
) {}