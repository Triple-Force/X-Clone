package logic_core.app.usecase.message;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.GetConversationMessagesRequest;
import logic_core.app.dto.response.ConversationMessagesResponse;
import logic_core.app.dto.response.MessageInfoResponse;
import logic_core.app.dto.validator.MessageValidator;
import logic_core.app.mapper.MessageMapper;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.SessionUserContext;
import logic_core.common.exception.ConflictException;
import logic_core.common.exception.ForbiddenException;
import logic_core.common.exception.NotFoundException;
import logic_core.common.exception.ValidationException;
import logic_core.common.result.Result;
import logic_core.domain.model.MessageModel;
import logic_core.domain.policy.DirectMessagePolicy;
import logic_core.domain.repository.DirectMessageRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetConversationMessagesUseCase
{
    @NonNull private final MessageValidator validator;
    @NonNull private final DirectMessagePolicy policy;
    @NonNull private final DirectMessageRepository repository;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<ConversationMessagesResponse> execute(
            GetConversationMessagesRequest request)
    {
        try
        {
            SessionUserContext context =
                    lockOrchestrator.lockAndGetContextByToken(
                            request.sessionToken()
                    );

            UUID actorId = context.lockedUser().getId();

            validator.validateGetConversationMessages(
                    actorId,
                    request.conversationId(),
                    request.page(),
                    request.pageSize()
            );

            policy.validateGetConversationMessages(
                    actorId,
                    request.conversationId()
            );

            List<MessageModel> messages =
                    repository.findByConversationId(
                            request.conversationId(),
                            request.page(),
                            request.pageSize()
                    );

            List<MessageInfoResponse> response =
                    messages.stream()
                            .map(MessageMapper::toResponse)
                            .toList();

            boolean hasMore =
                    response.size() == request.pageSize();

            return Result.success(
                    new ConversationMessagesResponse(
                            request.conversationId(),
                            response,
                            request.page(),
                            request.pageSize(),
                            hasMore
                    )
            );
        }
        catch (ValidationException |
               ForbiddenException |
               ConflictException |
               NotFoundException e)
        {
            return Result.failure(e.getMessage());
        }
    }
}