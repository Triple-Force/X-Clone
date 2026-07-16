package logic_core.app.dto.view;

import java.util.UUID;

public record PollOptionView(
        UUID id,
        String text,
        Long votesCount,
        boolean selected
) {}
