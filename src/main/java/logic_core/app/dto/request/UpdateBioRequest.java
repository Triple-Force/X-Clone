package logic_core.app.dto.request;

public record UpdateBioRequest(
        String sessionToken,
        String bio
) {}