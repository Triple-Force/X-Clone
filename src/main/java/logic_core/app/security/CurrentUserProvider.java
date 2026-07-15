package logic_core.app.security;

import Shared.Models.User.User;
import logic_core.common.exception.UnauthorizedException;
import logic_core.common.security.AuthPrincipal;

import java.util.Optional;
import java.util.UUID;

public class CurrentUserProvider
{
    public Optional<AuthPrincipal> getCurrentUser()
    {
        return Optional.ofNullable(AuthContext.getCurrentUser());
    }

    public AuthPrincipal requireCurrentUser()
    {
        return getCurrentUser()
                .orElseThrow(() ->  new UnauthorizedException("User is not authenticated."));
    }

    public UUID requireCurrentUserId()
    {
        return requireCurrentUser().userId();
    }
}
