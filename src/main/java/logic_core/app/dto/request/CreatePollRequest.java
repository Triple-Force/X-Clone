package logic_core.app.dto.request;

import java.util.List;

public record CreatePollRequest(
        String question,
        List<String> options,
        Integer durationHours
) {}
