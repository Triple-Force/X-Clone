package logic_core.app.dto.request;

public record LoginRequest(
        String username,
        String password
) {}
