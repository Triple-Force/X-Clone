package logic_core.app.dto.response;

import java.util.UUID;

public record PollOptionResponse(
        UUID optionId,
        String text,
        long voteCount
) {}
