package logic_core.app.dto.request;

public record SearchUsersRequest(
        String sessionToken,
        String query,
        int page,
        int pageSize
) {}