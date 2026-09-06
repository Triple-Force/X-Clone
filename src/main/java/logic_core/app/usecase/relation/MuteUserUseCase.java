package logic_core.app.usecase.relation;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.MuteUserRequest;
import logic_core.app.dto.response.MuteResponse;
import logic_core.app.dto.validator.MuteValidator;
import logic_core.app.mapper.MuteActionMapper;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.SessionUserContext;
import logic_core.common.exception.*;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.model.MuteRelation;
import logic_core.domain.policy.MutePolicy;
import logic_core.domain.repository.RelationshipRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MuteUserUseCase
{
    @NonNull private final MuteValidator validator;
    @NonNull private final MutePolicy policy;
    @NonNull private final RelationshipRepository relationshipRepository;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<MuteResponse> execute(MuteUserRequest request)
    {
        if (request == null || request.targetId() == null) {
            return Result.failure("Target user ID is required.");
        }

        try
        {
            SessionUserContext context =
                    lockOrchestrator.lockAndGetContextByToken(
                            request.sessionToken()
                    );

            UUID muterId = context.lockedUser().getId();
            UUID mutedId = request.targetId();

            validator.validate(muterId, mutedId);
            policy.validateMute(muterId, mutedId);

            MuteRelation muteRelation = MuteRelation.create(muterId, mutedId, timeProvider.now());
            relationshipRepository.saveMute(muteRelation);


            return Result.success(MuteActionMapper.toResponse(true));
        }
        catch (AppException e)
        {
            return Result.failure(e.getMessage());
        }
        catch (Exception e)
        {
            return Result.failure("Failed to process mute request due to a system error.");
        }
    }
}
