package logic_core.common.result;

import lombok.Getter;

@Getter
public final class CommandResult
{
    private final boolean success;
    private final String message;
    private final String errorCode;

    private CommandResult(boolean success, String message, String errorCode)
    {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static CommandResult success()
    {
        return new CommandResult(true, null, null);
    }

    public static CommandResult success(String message)
    {
        return new CommandResult(true, normalizeMessage(message), null);
    }

    public static CommandResult failure(String message)
    {
        return new CommandResult(false, normalizeMessage(message), null);
    }

    public static CommandResult failure(String message, String errorCode)
    {
        return new CommandResult(false, normalizeMessage(message), normalizeErrorCode(errorCode));
    }

    public boolean isFailure()
    {
        return !success;
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
}
