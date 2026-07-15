package logic_core.app.dto.request;

public record ChangePasswordRequest(
        String currentPassword, String newPassword
) {}
