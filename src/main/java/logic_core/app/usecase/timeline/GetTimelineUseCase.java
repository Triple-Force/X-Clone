package logic_core.app.usecase.timeline;

import logic_core.app.dto.request.GetTimelineRequest;
import logic_core.app.dto.request.GetTimelineResponse;
import logic_core.app.dto.timeline.TimelineTweet;
import logic_core.app.dto.validator.TimelineValidator;


import logic_core.common.exception.ConflictException;
import logic_core.common.exception.ForbiddenException;
import logic_core.common.exception.NotFoundException;
import logic_core.common.exception.ValidationException;
import logic_core.common.result.Result;

import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventPublisher;

import logic_core.domain.event.TimelineViewedEvent;
import logic_core.domain.policy.TimelinePolicy;
import logic_core.domain.repository.TweetRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetTimelineUseCase
{
    @NonNull private final TweetRepository tweetRepository;
    @NonNull private final TimelineValidator validator;
    @NonNull private final TimelinePolicy policy;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final TimeProvider timeProvider;

    public Result<GetTimelineResponse> execute(GetTimelineRequest request)
    {
        try
        {
            validator.validate(request.timelineType(), request.actorId(), request.page(), request.pageSize(), request.targetUserId());

            policy.validateTimeline(
                    request.timelineType(),
                    request.actorId(),
                    request.targetUserId()
            );

            int offset =
                    request.page() * request.pageSize();

            List<TimelineTweet> tweets =
                    tweetRepository.getTimeline(
                            request.timelineType(),
                            request.actorId(),
                            request.targetUserId(),
                            request.pageSize(),
                            offset
                    );

            long totalItems =
                    tweetRepository.countTimeline(
                            request.timelineType(),
                            request.actorId(),
                            request.targetUserId()
                    );

            boolean hasNext =
                    offset + tweets.size() < totalItems;

            GetTimelineResponse response =
                    GetTimelineResponse.builder()
                            .tweets(tweets)
                            .totalItems(totalItems)
                            .page(request.page())
                            .pageSize(request.pageSize())
                            .hasNext(hasNext)
                            .build();

            eventPublisher.publish(new TimelineViewedEvent(
                            request.actorId(),
                            request.targetUserId(),
                            request.timelineType(),
                            tweets.size(),
                            timeProvider.now()
                    )
            );

            return Result.success(response);
        }
        catch (ValidationException | ForbiddenException | ConflictException | NotFoundException e)
        {
            return Result.failure(e.getMessage());
        }
    }
}