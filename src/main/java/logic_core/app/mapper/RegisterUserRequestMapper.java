package logic_core.app.mapper;

import logic_core.app.dto.request.RegisterRequest;
import logic_core.domain.model.UserModel;

public class RegisterUserRequestMapper
{
    public static UserModel toModel(RegisterRequest request, String passwordHash)
    {
        if (request == null)
            return null;

        return UserModel.builder()
                .username(request.username())
                .email(request.email())
                .displayName(request.displayName())
                .passwordHash(passwordHash)
                .active(true)
                .verified(false)
                .deleted(false)
                .build();
    }
}