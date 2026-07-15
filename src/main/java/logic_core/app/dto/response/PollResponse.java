package logic_core.app.dto.response;

import logic_core.domain.model.PollOptionModel;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PollResponse(
        UUID pollId,
        UUID tweetId,
        UUID authorId,
        String question,
        List<PollOptionModel> options,
        OffsetDateTime createdAt,
        OffsetDateTime expirationDate,
        long totalVotes,
        boolean expired
) {}
