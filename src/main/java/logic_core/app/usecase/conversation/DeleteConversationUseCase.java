package logic_core.app.usecase.conversation;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.DeleteConversationRequest;
import logic_core.app.dto.response.DeleteConversationResponse;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteConversationUseCase
{
    @NonNull private final ConversationValidator validator;
    @NonNull private final ConversationPolicy policy;
    @NonNull private final ConversationRepository repository;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<DeleteConversationResponse> execute(DeleteConversationRequest request)
    {
        try
        {
            SessionUserContext context =
                    lockOrchestrator.lockAndGetContextByToken(
                            request.sessionToken()
                    );

            UUID actorId = context.lockedUser().getId();

            validator.validateDelete(actorId, request.conversationId());

            ConversationModel conversationModel = repository.findByIdForUpdate(request.conversationId())
                    .orElseThrow(() -> new NotFoundException("Conversation not found."));

            policy.validateDelete(actorId, conversationModel.getConversationId());

            repository.deleteById(conversationModel.getConversationId());


            return Result.success(new DeleteConversationResponse(conversationModel.getConversationId()));
        }
        catch (Exception e)
        {
            return Result.failure(e.getMessage());
        }
    }
}
