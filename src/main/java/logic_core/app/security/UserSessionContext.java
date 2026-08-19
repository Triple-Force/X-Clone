package logic_core.app.security;

import logic_core.common.security.AuthPrincipal;

import java.util.Optional;

public interface UserSessionContext
{
    void setAuthenticatedUser(AuthPrincipal principal);

    void clearAuthenticatedUser();

    Optional<AuthPrincipal> getAuthenticatedUser();

    default boolean isAuthenticated()
    {
        return getAuthenticatedUser().isPresent();
    }
}
