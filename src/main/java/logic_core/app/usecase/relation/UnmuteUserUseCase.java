package logic_core.app.usecase.relation;

import logic_core.app.dto.request.UnmuteUserRequest;
import logic_core.app.dto.response.MuteResponse;
import logic_core.app.dto.validator.MuteValidator;
import logic_core.app.mapper.MuteActionMapper;
import logic_core.app.security.CurrentUserProvider;
import logic_core.common.exception.ConflictException;
import logic_core.common.exception.OperationNotAllowedException;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.Relationship.UserUnmutedEvent;
import logic_core.domain.model.MuteRelation;
import logic_core.domain.policy.MutePolicy;
import logic_core.domain.repository.RelationshipRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UnmuteUserUseCase
{
    @NonNull private final MuteValidator validator;
    @NonNull private final MutePolicy policy;
    @NonNull private final RelationshipRepository relationshipRepository;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final CurrentUserProvider currentUserProvider;
    @NonNull private final TimeProvider timeProvider;

    public Result<MuteResponse> execute(UnmuteUserRequest request)
    {
        UUID unMuterId = currentUserProvider.requireCurrentUserId();
        UUID unmutedId = request.unmutedId();

        try
        {
            validator.validate(unMuterId, unmutedId);
            policy.validateUnmute(unMuterId, unmutedId);
        }
        catch (IllegalArgumentException | ConflictException | OperationNotAllowedException e)
        {
            return Result.failure(e.getMessage());
        }

        MuteRelation muteRelation = createMuteRelation(unMuterId, unmutedId);
        relationshipRepository.deleteMute(muteRelation);

        eventPublisher.publish(new UserUnmutedEvent(
                unMuterId,
                unmutedId,
                timeProvider.now()
        ));

        MuteResponse response = MuteActionMapper.toResponse(false);
        return Result.success(response);
    }

    private MuteRelation createMuteRelation(UUID unMuterId, UUID mutedId)
    {
        return MuteRelation.create(
                unMuterId,
                mutedId,
                timeProvider.now()
        );
    }
}
