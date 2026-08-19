package logic_core.app.dto.request;

import java.util.UUID;

public record PollOptionRequest(
        UUID optionId,
        UUID pollId,
        String text
) {}