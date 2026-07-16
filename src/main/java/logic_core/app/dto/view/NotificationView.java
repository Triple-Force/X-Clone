package logic_core.app.dto.view;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationView(
        UUID id,
        String type,
        UserCardView actor,
        String message,
        boolean read,
        OffsetDateTime createdAt
) {}
