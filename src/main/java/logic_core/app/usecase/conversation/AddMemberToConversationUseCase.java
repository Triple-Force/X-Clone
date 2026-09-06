package logic_core.app.usecase.conversation;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.AddConversationMemberRequest;
import logic_core.app.dto.response.ConversationInfoResponse;
import logic_core.app.dto.validator.ConversationValidator;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.SessionUserContext;
import logic_core.common.exception.*;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.model.ConversationModel;
import logic_core.domain.policy.ConversationPolicy;
import logic_core.domain.repository.ConversationRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddMemberToConversationUseCase
{
    @NonNull private final ConversationValidator validator;
    @NonNull private final ConversationPolicy policy;
    @NonNull private final ConversationRepository repository;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<ConversationInfoResponse> execute(AddConversationMemberRequest request)
    {
        try
        {
            SessionUserContext context =
                    lockOrchestrator.lockAndGetContextByToken(
                            request.sessionToken()
                    );

            UUID actorId = context.lockedUser().getId();

            validator.validateAddMember(actorId, request.memberId(), request.conversationId());
            policy.validateAddMember(actorId, request.memberId(), request.conversationId());

            ConversationModel conversationModel = repository.findByIdForUpdate(request.conversationId())
                    .orElseThrow(() -> new NotFoundException("Conversation not found."));

            repository.addMember(request.conversationId(), request.memberId());

            repository.update(conversationModel);
            OffsetDateTime now = timeProvider.now();


            return Result.success(new ConversationInfoResponse(
                    conversationModel.getConversationId(),
                    conversationModel.getCreatedAt(),
                    now,
                    conversationModel.getParticipantIds()
            ));
        }
        catch (Exception e)
        {
            return Result.failure(e.getMessage());
        }
    }
}
