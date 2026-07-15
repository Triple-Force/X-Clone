package logic_core.app.dto.request;

public record RegisterRequest(
        String username,
        String email,
        String password,
        String displayName
) {}