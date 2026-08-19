package logic_core.app.dto.response;

public record LogoutResponse(
        boolean success,
        String message
) {}
