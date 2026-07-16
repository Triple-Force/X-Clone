package logic_core.app.security;

import logic_core.common.security.AuthPrincipal;

import java.util.Optional;

public final class AuthContext
{
    private static final ThreadLocal<AuthPrincipal> CURRENT_PRINCIPAL =
            new ThreadLocal<>();

    private AuthContext()
    {
    }

    public static void setCurrentUser(AuthPrincipal principal)
    {
        if (principal == null)
        {
            throw new IllegalArgumentException("AuthPrincipal cannot be null.");
        }

        CURRENT_PRINCIPAL.set(principal);
    }

    public static AuthPrincipal getCurrentUser()
    {
        return CURRENT_PRINCIPAL.get();
    }

    public static Optional<AuthPrincipal> getCurrentUserOptional()
    {
        return Optional.ofNullable(CURRENT_PRINCIPAL.get());
    }

    public static AuthPrincipal requireCurrentUser()
    {
        AuthPrincipal principal = CURRENT_PRINCIPAL.get();

        if (principal == null)
        {
            throw new IllegalStateException("No authenticated user exists.");
        }

        return principal;
    }

    public static boolean isAuthenticated()
    {
        return CURRENT_PRINCIPAL.get() != null;
    }

    public static void clear()
    {
        CURRENT_PRINCIPAL.remove();
    }
}
