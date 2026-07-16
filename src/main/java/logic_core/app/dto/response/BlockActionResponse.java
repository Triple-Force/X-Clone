package logic_core.app.dto.response;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record BlockActionResponse(
        boolean success,
        boolean blocked,
        UUID blockerId,
        UUID blockedId,
        OffsetDateTime occurredAt
) {}
