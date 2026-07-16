package logic_core.app.usecase.relation;

import logic_core.app.dto.request.MuteUserRequest;
import logic_core.app.dto.response.MuteResponse;
import logic_core.app.dto.validator.MuteValidator;
import logic_core.app.mapper.MuteActionMapper;
import logic_core.app.security.CurrentUserProvider;
import logic_core.common.exception.ConflictException;
import logic_core.common.exception.OperationNotAllowedException;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.Relationship.UserMutedEvent;
import logic_core.domain.model.MuteRelation;
import logic_core.domain.policy.MutePolicy;
import logic_core.domain.repository.RelationshipRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class MuteUserUseCase
{
    @NonNull private final MuteValidator validator;
    @NonNull private final MutePolicy policy;
    @NonNull private final RelationshipRepository relationshipRepository;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final CurrentUserProvider currentUserProvider;
    @NonNull private final TimeProvider timeProvider;

    public Result<MuteResponse> execute(MuteUserRequest request)
    {
        UUID muterId = currentUserProvider.requireCurrentUserId();
        UUID mutedId = request.targetId();

        try
        {
            validator.validate(muterId, mutedId);
            policy.validateMute(muterId, mutedId);
        }
        catch (IllegalArgumentException | ConflictException | OperationNotAllowedException e)
        {
            return Result.failure(e.getMessage());
        }

        MuteRelation muteRelation = createMuteRelation(muterId, mutedId);
        relationshipRepository.saveMute(muteRelation);

        eventPublisher.publish(new UserMutedEvent(
                muterId,
                mutedId,
                timeProvider.now()
        ));

        MuteResponse response = MuteActionMapper.toResponse(true);
        return Result.success(response);
    }

    private MuteRelation createMuteRelation(UUID muterId, UUID mutedId)
    {
        return MuteRelation.create(
                muterId,
                mutedId,
                timeProvider.now()
        );
    }
}
