package Client;

public final class PasswordResetContext
{
    private static final PasswordResetContext INSTANCE = new PasswordResetContext();

    private String email;
    private String code;

    private PasswordResetContext()
    {
    }

    public static PasswordResetContext getInstance()
    {
        return INSTANCE;
    }

    public synchronized String getEmail()
    {
        return email;
    }

    public synchronized void setEmail(String email)
    {
        this.email = normalize(email);
    }

    public synchronized String getCode()
    {
        return code;
    }

    public synchronized void setCode(String code)
    {
        this.code = normalize(code);
    }

    public synchronized void clear()
    {
        email = null;
        code = null;
    }

    public synchronized boolean hasEmail()
    {
        return email != null && !email.isBlank();
    }

    public synchronized boolean hasCode()
    {
        return code != null && !code.isBlank();
    }

    private String normalize(String value)
    {
        if (value == null)
        {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
