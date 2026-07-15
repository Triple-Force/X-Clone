package logic_core.common.exception;

public class DatabaseException extends AppException
{
    public DatabaseException(String message)
    {
        super(message, "DATABASE_ERROR");
    }

    public DatabaseException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
