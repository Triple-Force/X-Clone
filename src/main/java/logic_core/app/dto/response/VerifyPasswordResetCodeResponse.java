package logic_core.app.dto.response;

public record VerifyPasswordResetCodeResponse(
        boolean verified,
        String message
) {}
