package logic_core.app.dto.request;

public record SearchUserRequest(String query,
         Integer page,
         Integer size
) {}