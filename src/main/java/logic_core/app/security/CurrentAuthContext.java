package logic_core.app.security;

import logic_core.common.exception.UnauthorizedException;
import logic_core.common.security.AuthPrincipal;

import java.util.UUID;

public class CurrentAuthContext
{
    public AuthPrincipal require()
    {
        return AuthContext.get()
                .orElseThrow(() ->
                        new UnauthorizedException("User is not authenticated."));
    }

    public UUID requireUserId()
    {
        return require().userId();
    }

    public UUID requireSessionId()
    {
        return require().sessionId();
    }

    public String requireSessionToken()
    {
        return require().sessionToken();
    }

    public String requireUsername()
    {
        return require().username();
    }
}