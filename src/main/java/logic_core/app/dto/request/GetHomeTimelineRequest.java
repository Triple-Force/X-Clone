package logic_core.app.dto.request;

public record GetHomeTimelineRequest(
        int limit,
        int offset
) {}