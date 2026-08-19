package logic_core.app.dto.request;

import logic_core.app.dto.timeline.TimelineTweet;
import lombok.Builder;

import java.util.List;

@Builder
public record GetTimelineResponse(
        List<TimelineTweet> tweets,
        long totalItems,
        int page,
        int pageSize,
        boolean hasNext
) {}
