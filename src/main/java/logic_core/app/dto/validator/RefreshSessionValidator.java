package logic_core.app.dto.validator;

public class RefreshSessionValidator
{
    public void validate(String refreshToken)
    {
        if (refreshToken == null || refreshToken.isBlank())
            throw new IllegalArgumentException("refresh_token.required");

        if (refreshToken.length() < 32)
            throw new IllegalArgumentException("refresh_token.invalid");

        if (refreshToken.length() > 512)
            throw new IllegalArgumentException("refresh_token.invalid");
    }
}
