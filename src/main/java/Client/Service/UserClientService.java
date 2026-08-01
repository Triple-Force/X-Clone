package Client.Service;

import Client.ClientApplicationContext;

import Client.session.ClientSession;
import Client.transport.SocketClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import logic_core.app.dto.media.UploadFile;
import logic_core.app.dto.request.*;
import logic_core.app.dto.response.*;
import logic_core.common.result.Result;
import logic_core.infrastructure.transport.RequestEnvelope;
import logic_core.infrastructure.transport.RequestType;
import logic_core.infrastructure.transport.ResponseEnvelope;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;


@RequiredArgsConstructor
public final class UserClientService
{
    private final SocketClient socketClient;
    private final ClientSession session;
    private final ExecutorService networkExecutor;
    private final Gson gson;


    public UserClientService(ClientApplicationContext context)
    {
        this(
                context.socketClient(),
                context.session(),
                context.networkExecutor(),
                new GsonBuilder().serializeNulls().create()

        );
    }


    public CompletableFuture<Result<ProfileInfoResponse>> getProfile(UUID userId)
    {



        GetProfileRequest request = new GetProfileRequest(session.getToken(), userId);

        return execute(
                RequestType.USER_GET_PROFILE,
                request,
                ProfileInfoResponse.class
        );
    }


    public CompletableFuture<Result<List<UserSearchResponse>>> searchUsers(String query, int limit, int offset)
    {
        SearchUsersRequest request = new SearchUsersRequest(session.getToken(), query, limit, offset);

        return executeList(
                RequestType.USER_SEARCH,
                request,
                UserSearchResponse.class
        );
    }


    public CompletableFuture<Result<Void>> updateProfile(String username,UUID userId, String displayName)
    {
        UpdateProfileRequest request = new UpdateProfileRequest(session.getToken(), userId, displayName, username);

        return executeVoid(
                RequestType.USER_UPDATE_PROFILE,
                request
        );
    }



    public CompletableFuture<Result<UpdateBioResponse>> updateBio(String bio)
    {
        UpdateBioRequest request = new UpdateBioRequest(session.getToken(), bio);

        return execute(
                RequestType.USER_UPDATE_BIO,
                request,
                UpdateBioResponse.class
        );
    }


    public CompletableFuture<Result<UpdateAvatarResponse>> updateAvatar(UploadFile avatar)
    {
        UpdateAvatarRequest request =
                new UpdateAvatarRequest(
                        session.getToken(),
                        avatar
                );

        return execute(
                RequestType.USER_UPDATE_AVATAR,
                request,
                UpdateAvatarResponse.class
        );
    }


    public CompletableFuture<Result<UpdateBannerResponse>> updateBanner(UploadFile banner)
    {
        UpdateBannerRequest request = new UpdateBannerRequest(session.getToken(), banner);

        return execute(
                RequestType.USER_UPDATE_BANNER,
                request,
                UpdateBannerResponse.class
        );
    }


    public CompletableFuture<Result<Void>> updateEmail(UUID userid, String email)
    {
        UpdateEmailRequest request = new UpdateEmailRequest(session.getToken(), userid, email);

        return executeVoid(
                RequestType.USER_UPDATE_EMAIL,
                request
        );
    }


    public CompletableFuture<Result<Void>> updatePassword(String oldPassword, String newPassword, UUID useId)
    {
        UpdatePasswordRequest request = new UpdatePasswordRequest(session.getToken(), useId, oldPassword, newPassword);

        return executeVoid(
                RequestType.USER_UPDATE_PASSWORD,
                request
        );
    }


    public CompletableFuture<Result<Void>> deleteAccount(String password, UUID userId)
    {
        DeleteAccountRequest request = new DeleteAccountRequest(userId,session.getToken(), password);

        return executeVoid(
                RequestType.USER_DELETE_ACCOUNT,
                request

        );
    }

    public CompletableFuture<Result<UpdateCompleteProfileResponse>> updateCompleteProfile(UUID userId, String displayName, String username, String bio,  UploadFile avatar, UploadFile banner)
    {
        UpdateCompleteProfileRequest request = new UpdateCompleteProfileRequest(session.getToken(), userId, displayName, username, bio,avatar, banner);

        return execute(
                RequestType.USER_UPDATE_COMPLETE_PROFILE,
                request,
                UpdateCompleteProfileResponse.class
        );
    }


    public CompletableFuture<Result<GetIsFollowingResponse>> isFollow(UUID targetId)
    {
        GetIsFollowingRequest request = new GetIsFollowingRequest(session.getToken(),targetId);

        return execute(
                RequestType.USER_GET_IS_FOLLOW,
                request,
                GetIsFollowingResponse.class
                );
    }

    public CompletableFuture<Result<GetIsLikedResponse>> isLike(UUID tweetId)
    {

        GetIsLikedRequest request = new GetIsLikedRequest(session.getToken(), tweetId);

        return execute(
                RequestType.USER_GET_IS_LIKE,
                request,
                GetIsLikedResponse.class
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



                if(response == null)
                {
                    return Result.failure("EMPTY_RESPONSE");
                }


                if(!response.isSuccess())
                {
                    return Result.failure(
                            response.errorMessage()
                    );
                }



                T data =
                        gson.fromJson(
                                response.getData(),
                                responseClass
                        );


                return Result.success(data);

            }
            catch(Exception e)
            {
                return Result.failure(e.getMessage());
            }

        }, networkExecutor);
    }


    private CompletableFuture<Result<Void>> executeVoid(
            RequestType type,
            Object request)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try
            {
                socketClient.connect();


                RequestEnvelope envelope =
                        new RequestEnvelope(
                                UUID.randomUUID(),
                                type,
                                gson.toJsonTree(request),
                                session.getToken()
                        );


                ResponseEnvelope response =
                        socketClient.send(envelope);


                if(response == null)
                {
                    return Result.failure("EMPTY_RESPONSE");
                }


                if(!response.isSuccess())
                {
                    return Result.failure(
                            response.errorMessage()
                    );
                }


                return Result.success(null);

            }
            catch(Exception e)
            {
                return Result.failure(e.getMessage());
            }

        }, networkExecutor);
    }


    private <T> CompletableFuture<Result<List<T>>> executeList(
            RequestType type,
            Object request,
            Class<T> elementClass)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try
            {
                socketClient.connect();


                RequestEnvelope envelope =
                        new RequestEnvelope(
                                UUID.randomUUID(),
                                type,
                                gson.toJsonTree(request),
                                session.getToken()
                        );


                ResponseEnvelope response =
                        socketClient.send(envelope);



                if(response == null)
                {
                    return Result.failure("EMPTY_RESPONSE");
                }


                if(!response.isSuccess())
                {
                    return Result.failure(
                            response.errorMessage()
                    );
                }



                List<T> data =
                        gson.fromJson(
                                response.getData(),
                                com.google.gson.reflect.TypeToken
                                        .getParameterized(
                                                List.class,
                                                elementClass
                                        )
                                        .getType()
                        );


                return Result.success(data);

            }
            catch(Exception e)
            {
                return Result.failure(e.getMessage());
            }

        }, networkExecutor);
    }

    private <T> CompletableFuture<Result<T>> executeAndCache(
            RequestType type,
            Object request,
            Class<T> responseClass,
            Consumer<T> cacheUpdater)
    {
        return execute(type, request, responseClass)
                .thenApply(result ->
                {
                    if (result.isSuccess())
                    {
                        cacheUpdater.accept(result.getData());
                    }

                    return result;
                });
    }


    private CompletableFuture<Result<Void>> executeVoidAndCache(
            RequestType type,
            Object request,
            Runnable cacheUpdater)
    {
        return executeVoid(type, request)
                .thenApply(result ->
                {
                    if (result.isSuccess())
                    {
                        cacheUpdater.run();
                    }

                    return result;
                });
    }
}