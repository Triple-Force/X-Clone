package logic_core.app.mapper;

import Shared.Models.Session.Session;
import logic_core.app.dto.response.AuthResponse;
import logic_core.domain.model.UserModel;

public final class AuthMapper
{

    private AuthMapper()
    {
    }

    public static AuthResponse toResponse(UserModel user, Session session)
    {
        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .token(session.getToken())
                .expiresAt(session.getExpiresAt())
                .message("User logged in successfully.")
                .sessionId(session.getId())
                .build();
    }
}
