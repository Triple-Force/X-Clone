package Client.Service;

import Client.ClientApplicationContext;
import Client.session.ClientSession;
import Client.transport.SocketClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import logic_core.app.dto.request.BlockUserRequest;
import logic_core.app.dto.request.FollowUserRequest;
import logic_core.app.dto.request.MuteUserRequest;
import logic_core.app.dto.request.UnblockUserRequest;
import logic_core.app.dto.request.UnfollowUserRequest;
import logic_core.app.dto.request.UnmuteUserRequest;
import logic_core.app.dto.response.BlockActionResponse;
import logic_core.app.dto.response.FollowResponse;
import logic_core.app.dto.response.MuteResponse;
import logic_core.common.result.Result;
import logic_core.infrastructure.transport.RequestEnvelope;
import logic_core.infrastructure.transport.RequestType;
import logic_core.infrastructure.transport.ResponseEnvelope;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
public final class RelationClientService
{
    private final SocketClient socketClient;
    private final ClientSession session;
    private final ExecutorService networkExecutor;
    private final Gson gson;

    public RelationClientService(ClientApplicationContext context)
    {
        this(
                context.socketClient(),
                context.session(),
                context.networkExecutor(),
                new GsonBuilder().serializeNulls().create()
        );
    }

    public CompletableFuture<Result<FollowResponse>> follow(UUID targetUserId)
    {
        FollowUserRequest request =
                new FollowUserRequest(
                        targetUserId,
                        session.getToken()
                );

        return execute(
                RequestType.RELATION_FOLLOW,
                request,
                FollowResponse.class
        );
    }

    public CompletableFuture<Result<FollowResponse>> unfollow(UUID targetUserId)
    {
        UnfollowUserRequest request =
                new UnfollowUserRequest(
                        targetUserId,
                        session.getToken()
                );

        return execute(
                RequestType.RELATION_UNFOLLOW,
                request,
                FollowResponse.class
        );
    }

    public CompletableFuture<Result<BlockActionResponse>> block(UUID targetUserId)
    {
        BlockUserRequest request =
                new BlockUserRequest(
                        targetUserId,
                        session.getToken()
                );

        return execute(
                RequestType.RELATION_BLOCK,
                request,
                BlockActionResponse.class
        );
    }

    public CompletableFuture<Result<BlockActionResponse>> unblock(UUID targetUserId)
    {
        UnblockUserRequest request =
                new UnblockUserRequest(
                        targetUserId,
                        session.getToken()
                );

        return execute(
                RequestType.RELATION_UNBLOCK,
                request,
                BlockActionResponse.class
        );
    }

    public CompletableFuture<Result<MuteResponse>> mute(UUID targetUserId)
    {
        MuteUserRequest request =
                new MuteUserRequest(
                        targetUserId,
                        session.getToken()
                );

        return execute(
                RequestType.RELATION_MUTE,
                request,
                MuteResponse.class
        );
    }

    public CompletableFuture<Result<MuteResponse>> unmute(UUID targetUserId)
    {
        UnmuteUserRequest request =
                new UnmuteUserRequest(
                        targetUserId,
                        session.getToken()
                );

        return execute(
                RequestType.RELATION_UNMUTE,
                request,
                MuteResponse.class
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

                JsonElement payload =
                        gson.toJsonTree(request);

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