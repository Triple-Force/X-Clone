package Client.Service;

import Client.ClientApplicationContext;
import Client.session.ClientSession;
import Client.transport.SocketClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import logic_core.app.dto.request.DeleteMediaRequest;
import logic_core.app.dto.request.DownloadMediaRequest;
import logic_core.app.dto.response.DownloadMediaResponse;
import logic_core.common.result.Result;
import logic_core.infrastructure.transport.RequestEnvelope;
import logic_core.infrastructure.transport.RequestType;
import logic_core.infrastructure.transport.ResponseEnvelope;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
public final class MediaClientService
{
    private final SocketClient socketClient;
    private final ClientSession session;
    private final ExecutorService networkExecutor;
    private final Gson gson;

    public MediaClientService(ClientApplicationContext context)
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

    public CompletableFuture<Result<Void>> deleteMedia(UUID mediaId)
    {
        DeleteMediaRequest request =
                new DeleteMediaRequest(
                        mediaId,
                        session.getToken()
                );

        return execute(
                RequestType.MEDIA_DELETE,
                request,
                Void.class
        );
    }

    public CompletableFuture<Result<DownloadMediaResponse>> downloadMedia(UUID mediaId)
    {
        DownloadMediaRequest request =
                new DownloadMediaRequest(
                        mediaId,
                        session.getToken()
                );

        return execute(
                RequestType.MEDIA_DOWNLOAD,
                request,
                DownloadMediaResponse.class
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

                if (responseClass == Void.class)
                {
                    return Result.success(null);
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