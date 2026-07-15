package logic_core.app.dto.request;

public record ResetPasswordRequest(
        String email,
        String code,
        String newPassword
) {}
