package logic_core.app.usecase.message;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.GetMessageRequest;
import logic_core.app.dto.response.MessageInfoResponse;
import logic_core.app.dto.validator.MessageValidator;
import logic_core.app.mapper.MessageMapper;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.CurrentAuthContext;
import logic_core.app.security.SessionUserContext;
import logic_core.common.exception.*;
import logic_core.common.result.Result;
import logic_core.domain.model.MessageModel;
import logic_core.domain.policy.DirectMessagePolicy;
import logic_core.domain.repository.DirectMessageRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class GetMessageUseCase
{
    @NonNull private final MessageValidator validator;
    @NonNull private final DirectMessagePolicy policy;
    @NonNull private final DirectMessageRepository repository;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    //readOnly = true
    @Transactional()
    public Result<MessageInfoResponse> execute(GetMessageRequest request)
    {
        try
        {
            SessionUserContext context =
                    lockOrchestrator.lockAndGetContextByToken(
                            request.sessionToken()
                    );

            UUID actorId = context.lockedUser().getId();

            validator.validateGetMessage(
                    actorId,
                    request.conversationId(),
                    request.messageId());

            policy.validateGetMessage(
                    actorId,
                    request.conversationId(),
                    request.messageId());

            MessageModel message = repository.findById(request.messageId())
                    .orElseThrow(() -> new NotFoundException("Message not found."));

            return Result.success(MessageMapper.toResponse(message));
        }
        catch (ValidationException | ForbiddenException | ConflictException | NotFoundException e)
        {
            return Result.failure(e.getMessage());
        }
    }
}
