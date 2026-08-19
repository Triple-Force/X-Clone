package logic_core.app.dto.view;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PollView(
        UUID id,
        String question,
        List<PollOptionView> options,
        boolean voted,
        boolean expired,
        OffsetDateTime expiresAt
) {}
