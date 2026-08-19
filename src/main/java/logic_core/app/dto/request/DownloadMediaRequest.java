package logic_core.app.dto.request;

import java.util.UUID;

public record DownloadMediaRequest(
        UUID mediaId,
        String sessionToken
) {}