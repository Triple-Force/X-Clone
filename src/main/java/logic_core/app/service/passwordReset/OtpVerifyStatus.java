package logic_core.app.service.passwordReset;

public enum OtpVerifyStatus
{
    OK,
    NOT_FOUND,
    EXPIRED,
    TOO_MANY_ATTEMPTS,
    INVALID_CODE
}

