package logic_core.infrastructure.transport;

public enum RequestType
{
    AUTH_REGISTER,
    AUTH_LOGIN,
    AUTH_LOGOUT,
    AUTH_REFRESH,
    AUTH_REQUEST_PASSWORD_RESET,
    AUTH_VERIFY_PASSWORD_RESET_CODE,
    AUTH_RESET_PASSWORD;

    public static RequestType fromWire(String value)
    {
        if (value == null || value.isBlank())
        {
            return null;
        }

        try
        {
            return RequestType.valueOf(value);
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

    public String toResponseWire()
    {
        return name() + "_RESPONSE";
    }
}
