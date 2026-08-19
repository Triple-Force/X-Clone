package logic_core.common.exception;

public class ForbiddenException extends AppException
{

    public ForbiddenException(String message)
    {
        super(message, "FORBIDDEN");
    }
}
