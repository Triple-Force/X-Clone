package logic_core.app.security;

import logic_core.common.security.AuthPrincipal;

import java.util.Optional;

public final class AuthContext
{
    private static final ThreadLocal<AuthPrincipal> CURRENT =
            new ThreadLocal<>();

    private AuthContext()
    {
    }

    public static void set(AuthPrincipal principal)
    {
        if (principal == null)
        {
            throw new IllegalArgumentException("AuthPrincipal cannot be null.");
        }

        CURRENT.set(principal);
    }

    public static Optional<AuthPrincipal> get()
    {
        return Optional.ofNullable(CURRENT.get());
    }

    public static AuthPrincipal require()
    {
        AuthPrincipal principal = CURRENT.get();

        if (principal == null)
        {
            throw new IllegalStateException("No authenticated user exists.");
        }

        return principal;
    }

    public static boolean isAuthenticated()
    {
        return CURRENT.get() != null;
    }

    public static void clear()
    {
        CURRENT.remove();
    }
}