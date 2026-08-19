package logic_core.app.dto.request;

public record SearchTweetRequest(
        String query,
        int page,
        int size
) {}
