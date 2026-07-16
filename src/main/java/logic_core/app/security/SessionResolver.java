package logic_core.app.security;

import Shared.Models.Session.Session;
import logic_core.common.security.AuthPrincipal;
import logic_core.session.SessionManager;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class SessionResolver
{
    private final SessionManager sessionManager;

    public Optional<AuthPrincipal> resolvePrincipal(String token)
    {
        if (token == null || token.isBlank())
        {
            return Optional.empty();
        }

        Optional<Session> sessionOpt = sessionManager.findValidSession(token);
        if (sessionOpt.isEmpty())
        {
            return Optional.empty();
        }

        Session session = sessionOpt.get();

        AuthPrincipal principal = new AuthPrincipal(
                session.getUser().getId(),
                session.getUser().getUsername(),
                session.getId()
        );

        return Optional.of(principal);
    }

    public void logout(String token)
    {
        if (token == null || token.isBlank())
        {
            return;
        }

        sessionManager.invalidateSessionByToken(token);
    }
}
