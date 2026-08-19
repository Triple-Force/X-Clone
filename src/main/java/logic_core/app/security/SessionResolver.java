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

        return sessionManager.findByToken(token)
                .filter(session -> session.getUser() != null)
                .map(this::toPrincipal);
    }

    private AuthPrincipal toPrincipal(Session session)
    {
        return new AuthPrincipal(
                session.getUser().getId(),
                session.getUser().getUsername(),
                session.getId(),
                session.getToken()
        );
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