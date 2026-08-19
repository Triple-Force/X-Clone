package logic_core.common.exception;

public class AlreadyExistsException extends AppException
{
    public AlreadyExistsException(String message)
    {
        super(message, "ALREADY_EXISTS");
    }
}
