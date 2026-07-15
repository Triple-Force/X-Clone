package logic_core.app.dto.response;

import java.util.UUID;

public record HashtagResponse(
        UUID id,
        String tag
) {}