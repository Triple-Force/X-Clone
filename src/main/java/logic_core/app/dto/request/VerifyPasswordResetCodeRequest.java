package logic_core.app.dto.request;

public record VerifyPasswordResetCodeRequest(
        String email,
        String code
) {}
