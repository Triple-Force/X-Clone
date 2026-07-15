package logic_core.infrastructure.transport;

public enum ResponseType
{
    AUTH_REGISTER_RESPONSE,
    AUTH_LOGIN_RESPONSE,
    AUTH_LOGOUT_RESPONSE,
    AUTH_REFRESH_RESPONSE,

    UNKNOWN_REQUEST,
    BAD_REQUEST;

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
