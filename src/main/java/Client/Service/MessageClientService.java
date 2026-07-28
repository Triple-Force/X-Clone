package Client.Service;

import Client.ClientApplicationContext;
import Client.session.ClientSession;
import Client.transport.SocketClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import logic_core.app.dto.request.DeleteMessageRequest;
import logic_core.app.dto.request.EditMessageRequest;
import logic_core.app.dto.request.GetConversationMessagesRequest;
import logic_core.app.dto.request.GetMessageRequest;
import logic_core.app.dto.request.SendMessageRequest;
import logic_core.app.dto.response.ConversationMessagesResponse;
import logic_core.app.dto.response.ConversationStateResponse;
import logic_core.app.dto.response.MessageInfoResponse;
import logic_core.common.result.Result;
import logic_core.infrastructure.transport.RequestEnvelope;
import logic_core.infrastructure.transport.RequestType;
import logic_core.infrastructure.transport.ResponseEnvelope;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
public final class MessageClientService
{
    private final SocketClient socketClient;
    private final ClientSession session;
    private final ExecutorService networkExecutor;
    private final Gson gson;

    public MessageClientService(ClientApplicationContext context)
    {
        this(
                context.socketClient(),
                context.session(),
                context.networkExecutor(),
                new GsonBuilder().serializeNulls().create()
        );
    }

    public CompletableFuture<Result<ConversationStateResponse>> sendMessage(
            UUID conversationId,
            String content)
    {
        SendMessageRequest request = new SendMessageRequest(conversationId, content, session.getToken());

        return execute(
                RequestType.MESSAGE_SEND,
                request,
                ConversationStateResponse.class
        );
    }

    public CompletableFuture<Result<ConversationStateResponse>> editMessage(
            UUID conversationId,
            UUID messageId,
            String newContent)
    {
        EditMessageRequest request = new EditMessageRequest(conversationId, messageId, newContent, session.getToken());

        return execute(
                RequestType.MESSAGE_EDIT,
                request,
                ConversationStateResponse.class
        );
    }

    public CompletableFuture<Result<ConversationStateResponse>> deleteMessage(
            UUID conversationId,
            UUID messageId)
    {
        DeleteMessageRequest request = new DeleteMessageRequest(conversationId, messageId, session.getToken());

        return execute(
                RequestType.MESSAGE_DELETE,
                request,
                ConversationStateResponse.class
        );
    }

    public CompletableFuture<Result<MessageInfoResponse>> getMessage(
            UUID conversationId,
            UUID messageId)
    {
        GetMessageRequest request = new GetMessageRequest(conversationId, messageId, session.getToken());

        return execute(
                RequestType.MESSAGE_GET,
                request,
                MessageInfoResponse.class
        );
    }

    public CompletableFuture<Result<ConversationMessagesResponse>> getConversationMessages(
            UUID conversationId,
            int page,
            int pageSize)
    {

        GetConversationMessagesRequest request = new GetConversationMessagesRequest(conversationId, page, pageSize, session.getToken()
                );

        return execute(
                RequestType.MESSAGE_GET_CONVERSATION,
                request,
                ConversationMessagesResponse.class
        );
    }

    private <T> CompletableFuture<Result<T>> execute(
            RequestType type,
            Object request,
            Class<T> responseClass)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try
            {
                socketClient.connect();

                JsonElement payload = gson.toJsonTree(request);

                RequestEnvelope envelope =
                        new RequestEnvelope(
                                UUID.randomUUID(),
                                type,
                                payload,
                                session.getToken()
                        );

                ResponseEnvelope response =
                        socketClient.send(envelope);

                if (response == null)
                {
                    return Result.failure("EMPTY_RESPONSE");
                }

                if (!response.isSuccess())
                {
                    return Result.failure(response.errorMessage());
                }

                T data =
                        gson.fromJson(
                                response.getData(),
                                responseClass
                        );

                return Result.success(data);
            }
            catch (Exception e)
            {
                return Result.failure(e.getMessage());
            }
        }, networkExecutor);
    }
}