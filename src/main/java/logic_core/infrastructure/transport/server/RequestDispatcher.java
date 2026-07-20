package logic_core.infrastructure.transport.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import logic_core.app.DependencyContainer;
import logic_core.app.dto.request.*;
import logic_core.app.dto.response.*;
import logic_core.app.facade.AuthFacade;
import logic_core.common.exception.AppException;
import logic_core.common.result.Result;
import logic_core.infrastructure.transport.RequestEnvelope;
import logic_core.infrastructure.transport.RequestType;
import logic_core.infrastructure.transport.ResponseEnvelope;
import logic_core.infrastructure.transport.ResponseType;

import java.util.Objects;
import java.util.UUID;

public class RequestDispatcher
{
    private final Gson gson;

    public RequestDispatcher(Gson gson)
    {
        this.gson = Objects.requireNonNull(gson, "gson must not be null");
    }

    public ResponseEnvelope dispatch(RequestEnvelope requestEnvelope)
    {
        Objects.requireNonNull(requestEnvelope, "requestEnvelope must not be null");

        UUID requestId = requestEnvelope.requestId();
        RequestType requestType = requestEnvelope.type();
        JsonElement payload = requestEnvelope.payload();

        EntityManager em = null;
        EntityTransaction tx = null;

        try
        {
            em = DependencyContainer.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            AuthFacade authFacade = DependencyContainer.createAuthFacade(em);

            ResponseEnvelope response = switch (requestType)
            {
                case AUTH_REGISTER -> handleRegister(requestId, payload, authFacade);
                case AUTH_LOGIN -> handleLogin(requestId, payload, authFacade);
                case AUTH_LOGOUT -> handleLogout(requestId, payload, authFacade);
                case AUTH_REFRESH -> handleRefresh(requestId, payload, authFacade);
                case AUTH_REQUEST_PASSWORD_RESET -> handelRequestPasswordReset(requestId, payload, authFacade);
                case AUTH_VERIFY_PASSWORD_RESET_CODE -> handelVerifyPasswordResetCode(requestId, payload, authFacade);
                case AUTH_RESET_PASSWORD -> handelResetPassword(requestId, payload, authFacade);
            };

            tx.commit();
            return response;

        }
        catch (AppException e)
        {
            rollbackQuietly(tx);
            return failureResponse(
                    requestId,
                    responseTypeFor(requestType),
                    "APP_ERROR",
                    e.getMessage()
            );

        }
        catch (Exception e)
        {
            rollbackQuietly(tx);
            return failureResponse(
                    requestId,
                    ResponseType.BAD_REQUEST,
                    "UNEXPECTED_ERROR",
                    e.getMessage() != null ? e.getMessage() : "Unexpected server error"
            );


        } finally
        {
            closeQuietly(em);
        }
    }

    private ResponseEnvelope handleRegister(
            UUID requestId,
            JsonElement payload,
            AuthFacade authFacade
    )
    {
        RegisterRequest request =
                gson.fromJson(payload, RegisterRequest.class);

        Result<AuthResponse> result = authFacade.register(request);

        if (result.isFailure())
        {
            return failureResponse(
                    requestId,
                    ResponseType.AUTH_REGISTER_RESPONSE,
                    "AUTH_REGISTER_FAILED",
                    result.getError()
            );
        }

        return successResponse(
                requestId,
                ResponseType.AUTH_REGISTER_RESPONSE,
                result.getData()
        );
    }

    private ResponseEnvelope handleLogin(
            UUID requestId,
            JsonElement payload,
            AuthFacade authFacade
    )
    {
        LoginRequest request =
                gson.fromJson(payload, LoginRequest.class);

        Result<AuthResponse> result = authFacade.login(request);

        if (result.isFailure())
        {
            return failureResponse(
                    requestId,
                    ResponseType.AUTH_LOGIN_RESPONSE,
                    "AUTH_LOGIN_FAILED",
                    result.getError()
            );
        }

        return successResponse(
                requestId,
                ResponseType.AUTH_LOGIN_RESPONSE,
                result.getData()
        );
    }

    private ResponseEnvelope handleLogout(
            UUID requestId,
            JsonElement payload,
            AuthFacade authFacade
    )
    {
        LogoutRequest request =
                gson.fromJson(payload, LogoutRequest.class);

        Result<LogoutResponse> result = authFacade.logout(request);

        if (result.isFailure())
        {
            return failureResponse(
                    requestId,
                    ResponseType.AUTH_LOGOUT_RESPONSE,
                    "AUTH_LOGOUT_FAILED",
                    result.getError()
            );
        }

        return successResponse(
                requestId,
                ResponseType.AUTH_LOGOUT_RESPONSE,
                result.getData()
        );
    }

    private ResponseEnvelope handleRefresh(
            UUID requestId,
            JsonElement payload,
            AuthFacade authFacade
    )
    {
        RefreshSessionRequest request =
                gson.fromJson(payload, RefreshSessionRequest.class);

        Result<AuthResponse> result = authFacade.refresh(request);

        if (result.isFailure())
        {
            return failureResponse(
                    requestId,
                    ResponseType.AUTH_REFRESH_RESPONSE,
                    "AUTH_REFRESH_FAILED",
                    result.getError()
            );
        }

        return successResponse(
                requestId,
                ResponseType.AUTH_REFRESH_RESPONSE,
                result.getData()
        );
    }

    private ResponseEnvelope handelRequestPasswordReset(
            UUID requestId,
            JsonElement payload,
            AuthFacade authFacade
    )
    {
        RequestPasswordResetRequest request =
                gson.fromJson(payload, RequestPasswordResetRequest.class);

        Result<RequestPasswordResetResponse> result = authFacade.requestPasswordReset(request);

        return successResponse(
                requestId,
                ResponseType.AUTH_REQUEST_PASSWORD_RESET_RESPONSE,
                result.getData()
        );
    }

    private ResponseEnvelope handelVerifyPasswordResetCode(
            UUID requestId,
            JsonElement payload,
            AuthFacade authFacade
    )
    {
        VerifyPasswordResetCodeRequest request =
                gson.fromJson(payload, VerifyPasswordResetCodeRequest.class);

        Result<VerifyPasswordResetCodeResponse> result = authFacade.verifyPasswordResetCode(request);

        return successResponse(
                requestId,
                ResponseType.AUTH_VERIFY_PASSWORD_RESET_CODE_RESPONSE,
                result.getData()
        );
    }

    private ResponseEnvelope handelResetPassword(
            UUID requestId,
            JsonElement payload,
            AuthFacade authFacade
    )
    {
        ResetPasswordRequest request =
                gson.fromJson(payload, ResetPasswordRequest.class);

        Result<ResetPasswordResponse> result = authFacade.resetPassword(request);

        return successResponse(
                requestId,
                ResponseType.AUTH_RESET_PASSWORD_RESPONSE,
                result.getData()
        );
    }

    private ResponseEnvelope successResponse(UUID requestId, ResponseType type, Object body)
    {
        return ResponseEnvelope.success(
                requestId,
                type.toWire(),
                gson.toJsonTree(body)
        );
    }

    private ResponseEnvelope failureResponse(
            UUID requestId,
            ResponseType type,
            String errorCode,
            String errorMessage
    )
    {
        return ResponseEnvelope.failure(
                requestId,
                type.toWire(),
                errorCode,
                errorMessage
        );
    }

    private ResponseType responseTypeFor(RequestType requestType)
    {
        return switch (requestType)
        {
            case AUTH_REGISTER -> ResponseType.AUTH_REGISTER_RESPONSE;
            case AUTH_LOGIN -> ResponseType.AUTH_LOGIN_RESPONSE;
            case AUTH_LOGOUT -> ResponseType.AUTH_LOGOUT_RESPONSE;
            case AUTH_REFRESH -> ResponseType.AUTH_REFRESH_RESPONSE;
            case AUTH_REQUEST_PASSWORD_RESET -> ResponseType.AUTH_REQUEST_PASSWORD_RESET_RESPONSE;
            case AUTH_VERIFY_PASSWORD_RESET_CODE -> ResponseType.AUTH_VERIFY_PASSWORD_RESET_CODE_RESPONSE;
            case AUTH_RESET_PASSWORD -> ResponseType.AUTH_RESET_PASSWORD_RESPONSE;
        };
    }

    private void rollbackQuietly(EntityTransaction tx) {
        if (tx != null && tx.isActive())
        {
            tx.rollback();
        }
    }

    private void closeQuietly(EntityManager em)
    {
        if (em != null && em.isOpen())
        {
            em.close();
        }
    }
}
