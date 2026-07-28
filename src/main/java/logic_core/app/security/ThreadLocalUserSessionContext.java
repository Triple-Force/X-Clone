package logic_core.app.security;

import logic_core.common.security.AuthPrincipal;

import java.util.Optional;

public class ThreadLocalUserSessionContext implements UserSessionContext
{
    @Override
    public Optional<AuthPrincipal> getAuthenticatedUser()
    {
        return AuthContext.get();
    }

    @Override
    public void setAuthenticatedUser(AuthPrincipal principal)
    {
        AuthContext.set(principal);
    }

    @Override
    public void clearAuthenticatedUser()
    {
        AuthContext.clear();
    }
}