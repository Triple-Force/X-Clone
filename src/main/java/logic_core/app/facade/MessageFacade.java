package logic_core.app.facade;

import logic_core.app.dto.request.*;
import logic_core.app.dto.response.ConversationMessagesResponse;
import logic_core.app.dto.response.ConversationStateResponse;
import logic_core.app.dto.response.MessageInfoResponse;
import logic_core.app.usecase.message.*;
import logic_core.common.result.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageFacade
{
    private final DeleteMessageUseCase deleteMessageUseCase;
    private final EditMessageUseCase editMessageUseCase;
    private final GetConversationMessagesUseCase getConversationMessagesUseCase;
    private final GetMessageUseCase getMessageUseCase;
    private final SendMessageUseCase sendMessageUseCase;

    public Result<ConversationStateResponse> deleteMessage(DeleteMessageRequest request)
    {
        return deleteMessageUseCase.execute(request);
    }

    public Result<ConversationStateResponse> editMessage(EditMessageRequest request)
    {
        return editMessageUseCase.execute(request);
    }

    public Result<ConversationMessagesResponse> getConversationMessages(GetConversationMessagesRequest request)
    {
        return getConversationMessagesUseCase.execute(request);
    }

    public Result<MessageInfoResponse> getMessage(GetMessageRequest request)
    {
        return getMessageUseCase.execute(request);
    }

    public Result<ConversationStateResponse> sendMessage(SendMessageRequest request)
    {
        return sendMessageUseCase.execute(request);
    }
}
