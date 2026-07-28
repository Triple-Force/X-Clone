package logic_core.app.facade;

import logic_core.app.dto.request.GetTimelineRequest;
import logic_core.app.dto.request.GetTimelineResponse;
import logic_core.app.usecase.timeline.GetTimelineUseCase;
import logic_core.common.result.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TimelineFacade
{
    @NonNull private final GetTimelineUseCase getTimelineUseCase;

    public Result<GetTimelineResponse> getTimeline(GetTimelineRequest request)
    {
        return getTimelineUseCase.execute(request);
    }
}
