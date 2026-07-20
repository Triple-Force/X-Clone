package logic_core.common.exception;


import lombok.Getter;

@Getter
public class AuthApiException extends AppException
{
    private final String errorCode;

    public AuthApiException(String message, String errorCode)
    {
        super(message, "AuthApi");
        this.errorCode = errorCode;
    }

}
