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
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.SessionUserContext;

import logic_core.domain.policy.TimelinePolicy;
import logic_core.domain.repository.TweetRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetTimelineUseCase
{
    @NonNull private final TweetRepository tweetRepository;
    @NonNull private final TimelineValidator validator;
    @NonNull private final TimelinePolicy policy;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    public Result<GetTimelineResponse> execute(GetTimelineRequest request)
    {
        try
        {
            // The actor must be the authenticated session user; the caller-supplied
            // actorId field is intentionally not trusted (prevents reading another
            // user's HOME/FOLLOWING timeline by spoofing their id).
            SessionUserContext context =
                    lockOrchestrator.lockAndGetContextByToken(
                            request.sessionToken()
                    );

            UUID actorId = context.lockedUser().getId();

            validator.validate(request.timelineType(), actorId, request.page(), request.pageSize(), request.targetUserId());

            policy.validateTimeline(
                    request.timelineType(),
                    actorId,
                    request.targetUserId()
            );

            int offset =
                    request.page() * request.pageSize();

            List<TimelineTweet> tweets =
                    tweetRepository.getTimeline(
                            request.timelineType(),
                            actorId,
                            request.targetUserId(),
                            request.pageSize(),
                            offset
                    );

            long totalItems =
                    tweetRepository.countTimeline(
                            request.timelineType(),
                            actorId,
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


            return Result.success(response);
        }
        catch (ValidationException | ForbiddenException | ConflictException | NotFoundException e)
        {
            return Result.failure(e.getMessage());
        }
    }
}