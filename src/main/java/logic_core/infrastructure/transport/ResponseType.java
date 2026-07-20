package logic_core.infrastructure.transport;

public enum ResponseType
{
    AUTH_REGISTER_RESPONSE,
    AUTH_LOGIN_RESPONSE,
    AUTH_LOGOUT_RESPONSE,
    AUTH_REFRESH_RESPONSE,
    UNKNOWN_REQUEST,
    BAD_REQUEST,
    AUTH_REQUEST_PASSWORD_RESET_RESPONSE,
    AUTH_VERIFY_PASSWORD_RESET_CODE_RESPONSE,
    AUTH_RESET_PASSWORD_RESPONSE;

    public static ResponseType fromWire(String value)
    {
        if (value == null || value.isBlank())
        {
            return null;
        }

        try
        {
            return ResponseType.valueOf(value);
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    public String toWire()
    {
        return name();
    }
}
