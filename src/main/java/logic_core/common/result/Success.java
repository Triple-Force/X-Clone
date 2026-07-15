package logic_core.common.result;

import lombok.Getter;

@Getter
public final class Success<T> extends Result<T>
{
    private final T data;

    public Success(T data)
    {
        super(true, null);
        this.data = data;
    }

    public Success(T data, String message)
    {
        super(true, message);
        this.data = data;
    }

}
