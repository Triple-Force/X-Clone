package logic_core.app.usecase.relation;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.UnmuteUserRequest;
import logic_core.app.dto.response.MuteResponse;
import logic_core.app.dto.validator.MuteValidator;
import logic_core.app.mapper.MuteActionMapper;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.SessionUserContext;
import logic_core.common.exception.AppException;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.policy.MutePolicy;
import logic_core.domain.repository.RelationshipRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UnmuteUserUseCase
{
    @NonNull private final MuteValidator validator;
    @NonNull private final MutePolicy policy;
    @NonNull private final RelationshipRepository relationshipRepository;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<MuteResponse> execute(UnmuteUserRequest request)
    {
        if (request == null || request.unmutedId() == null) {
            return Result.failure("Unmuted ID is required.");
        }

        try
        {
            SessionUserContext context =
                    lockOrchestrator.lockAndGetContextByToken(
                            request.sessionToken()
                    );

            UUID unMuterId = context.lockedUser().getId();
            UUID unmutedId = request.unmutedId();

            validator.validate(unMuterId, unmutedId);
            policy.validateUnmute(unMuterId, unmutedId);

            relationshipRepository.findMuteRelation(unMuterId, unmutedId)
                    .ifPresent(relationshipRepository::deleteMute);


            return Result.success(MuteActionMapper.toResponse(false));
        }
        catch (AppException e)
        {
            return Result.failure(e.getMessage());
        }
        catch (Exception e)
        {
            return Result.failure("Failed to unmute user due to a system error.");
        }
    }
}
