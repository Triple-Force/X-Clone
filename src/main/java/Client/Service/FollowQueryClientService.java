package Client.Service;

import Client.ClientApplicationContext;
import Client.session.ClientSession;
import Client.transport.SocketClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import logic_core.app.dto.request.GetFollowersRequest;
import logic_core.app.dto.request.GetFollowingsRequest;
import logic_core.app.dto.response.FollowingsResponse;
import logic_core.app.dto.response.GetFollowersResponse;
import logic_core.common.result.Result;
import logic_core.infrastructure.transport.RequestEnvelope;
import logic_core.infrastructure.transport.RequestType;
import logic_core.infrastructure.transport.ResponseEnvelope;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
public final class FollowQueryClientService
{
    private final SocketClient socketClient;
    private final ClientSession session;
    private final ExecutorService networkExecutor;
    private final Gson gson;

    public FollowQueryClientService(ClientApplicationContext context)
    {
        this(
                context.socketClient(),
                context.session(),
                context.networkExecutor(),
                new GsonBuilder()
                        .serializeNulls()
                        .create()
        );
    }

    public CompletableFuture<Result<FollowingsResponse>> getFollowings(
            UUID targetUserId)
    {
        GetFollowingsRequest request =
                new GetFollowingsRequest(
                        targetUserId,
                        session.getToken()
                );

        return execute(
                RequestType.FOLLOW_GET_FOLLOWINGS,
                request,
                FollowingsResponse.class
        );
    }

    public CompletableFuture<Result<GetFollowersResponse>> getFollowers(
            UUID targetUserId)
    {
        GetFollowersRequest request = new GetFollowersRequest(session.getToken(), targetUserId);

        return execute(
                RequestType.FOLLOW_GET_FOLLOWERS,
                request,
                GetFollowersResponse.class
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