package logic_core.common.result;

import lombok.Getter;

@Getter
public final class Failure<T> extends Result<T>
{
    private final String errorCode;

    public Failure(String message)
    {
        super(false, message);
        this.errorCode = null;
    }

    public Failure(String message, String errorCode)
    {
        super(false, message);
        this.errorCode = errorCode;
    }

    @Override
    public T getData()
    {
        return null;
    }
}
