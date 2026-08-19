package logic_core.common.exception;

public class OperationNotAllowedException extends AppException
{
    public OperationNotAllowedException(String message)
    {
        super(message, "OPERATION_NOT_ALLOWED");
    }
}
