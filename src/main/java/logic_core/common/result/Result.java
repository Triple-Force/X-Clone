package logic_core.common.result;


public abstract class Result<T>
{
    private final boolean success;
    private final String message;

    protected Result(boolean success, String message)
    {
        this.success = success;
        this.message = message;
    }

    public abstract T getData();


    public final boolean isSuccess()
    {
        return success;
    }

    public final boolean isFailure()
    {
        return !success;
    }

    public final String getMessage()
    {
        return message;
    }

    // -------------------------
    // Factory methods
    // -------------------------

    public static <T> Result<T> success(T data)
    {
        return new Success<>(data);
    }

    public static <T> Result<T> success(T data, String message)
    {
        return new Success<>(data, normalizeMessage(message));
    }

    public static <T> Result<T> failure(String message)
    {
        return new Failure<>(normalizeMessage(message));
    }

    public static <T> Result<T> failure(String message, String errorCode)
    {
        return new Failure<>(normalizeMessage(message), normalizeErrorCode(errorCode));
    }

    private static String normalizeMessage(String message)
    {
        if (message == null) return null;
        String trimmed = message.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeErrorCode(String errorCode)
    {
        if (errorCode == null) return null;
        String trimmed = errorCode.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public final String getError()
    {
        return isFailure() ? message : null;
    }

}
