package logic_core.app.facade;

import logic_core.app.dto.request.*;
import logic_core.app.dto.response.ConversationMessagesResponse;
import logic_core.app.dto.response.ConversationStateResponse;
import logic_core.app.dto.response.MessageInfoResponse;
import logic_core.app.usecase.message.*;
import logic_core.common.result.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageFacade
{
    @NonNull private final DeleteMessageUseCase deleteMessageUseCase;
    @NonNull private final EditMessageUseCase editMessageUseCase;
    @NonNull private final GetConversationMessagesUseCase getConversationMessagesUseCase;
    @NonNull private final GetMessageUseCase getMessageUseCase;
    @NonNull private final SendMessageUseCase sendMessageUseCase;

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
