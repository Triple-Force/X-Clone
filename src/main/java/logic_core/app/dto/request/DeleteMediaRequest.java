package logic_core.app.dto.request;

import java.util.UUID;

public record DeleteMediaRequest(
        UUID mediaId,
        String sessionToken
) {}