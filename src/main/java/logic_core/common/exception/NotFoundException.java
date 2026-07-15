package logic_core.common.exception;

public class NotFoundException extends AppException
{
    public NotFoundException(String message)
    {
        super(message, "NOT_FOUND");
    }
}
