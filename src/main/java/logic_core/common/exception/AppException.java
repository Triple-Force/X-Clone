package logic_core.common.exception;

public class AppException extends RuntimeException
{
    private final String errorCode;

    public AppException(String message)
    {
        super(message);
        this.errorCode = null;
    }

    public AppException(String message, String errorCode)
    {
        super(message);
        this.errorCode = errorCode;
    }

    public AppException(String message, Throwable cause)
    {
        super(message, cause);
        this.errorCode = null;
    }

    public String getErrorCode()
    {
        return errorCode;
    }
}
