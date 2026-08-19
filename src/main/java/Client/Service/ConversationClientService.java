package Client.Service;

import Client.ClientApplicationContext;
import Client.session.ClientSession;
import Client.transport.SocketClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import logic_core.app.dto.request.*;
import logic_core.app.dto.response.ConversationInfoResponse;
import logic_core.app.dto.response.CreateConversationResponse;
import logic_core.app.dto.response.DeleteConversationResponse;
import logic_core.app.dto.response.GetConversationsResponse;
import logic_core.common.result.Result;
import logic_core.infrastructure.transport.RequestEnvelope;
import logic_core.infrastructure.transport.RequestType;
import logic_core.infrastructure.transport.ResponseEnvelope;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
public final class ConversationClientService
{
    private final SocketClient socketClient;
    private final ClientSession session;
    private final ExecutorService networkExecutor;
    private final Gson gson;

    public ConversationClientService(ClientApplicationContext context)
    {
        this(
                context.socketClient(),
                context.session(),
                context.networkExecutor(),
                new GsonBuilder().serializeNulls().create()
        );
    }

    public CompletableFuture<Result<ConversationInfoResponse>> addMember(
            UUID conversationId,
            UUID memberId)
    {

        AddConversationMemberRequest request = new AddConversationMemberRequest(conversationId, memberId, session.getToken());
        return execute(
                RequestType.MEMBER_ADD,
                request,
                ConversationInfoResponse.class
        );
    }

    public CompletableFuture<Result<CreateConversationResponse>> createConversation(
            UUID creatorId,
            List<UUID> participantIds)
    {
        CreateConversationRequest request = new CreateConversationRequest(creatorId, participantIds, session.getToken());

        return execute(
                RequestType.CONVERSATION_CREATE,
                request,
                CreateConversationResponse.class
        );
    }


    public CompletableFuture<Result<ConversationInfoResponse>> removeMember(
            UUID memberId,
            UUID conversationId)
    {
        RemoveConversationMemberRequest request = new RemoveConversationMemberRequest(memberId, conversationId, session.getToken());

        return execute(
                RequestType.MEMBER_DELETE,
                request,
                ConversationInfoResponse.class
        );
    }

    public CompletableFuture<Result<DeleteConversationResponse>> deleteConversation(
            UUID conversationId,
            UUID deleterId)
    {
        DeleteConversationRequest request = new DeleteConversationRequest(conversationId, deleterId , session.getToken());

        return execute(
                RequestType.CONVERSATION_DELETE,
                request,
                DeleteConversationResponse.class
        );
    }

    public CompletableFuture<Result<GetConversationsResponse>> getConversations(
            int page,
            int pageSize)
    {
        GetConversationsRequest request = GetConversationsRequest.builder()
                .page(page)
                .pageSize(pageSize)
                .sessionToken(session.getToken())
                .build();

        return execute(
                RequestType.CONVERSATION_GET,
                request,
                GetConversationsResponse.class
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
                        gson.fromJson(response.getData(), responseClass);

                return Result.success(data);
            }
            catch (Exception e)
            {
                return Result.failure(e.getMessage());
            }
        }, networkExecutor);
    }
}