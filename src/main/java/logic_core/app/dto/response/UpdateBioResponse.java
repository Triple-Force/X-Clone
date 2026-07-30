package logic_core.app.dto.response;

import java.util.UUID;

public record UpdateBioResponse(
        UUID userId,
        String bio
) {}