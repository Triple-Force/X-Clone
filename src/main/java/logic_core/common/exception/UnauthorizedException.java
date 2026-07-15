package logic_core.common.exception;

public class UnauthorizedException extends AppException
{
    public UnauthorizedException(String message)
    {
        super(message, "UNAUTHORIZED");
    }
}
