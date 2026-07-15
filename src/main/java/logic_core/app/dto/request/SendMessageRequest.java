package logic_core.app.dto.request;

import java.util.UUID;

public record SendMessageRequest(
        UUID receiverUserId,
        String text
) {}