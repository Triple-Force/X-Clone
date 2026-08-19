package logic_core.domain.model;

import Shared.Models.Tweet.Tweet;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PollModel
{
    private UUID pollId;
    private Tweet tweet;
    private UUID authorId;
    private String question;
    private List<PollOptionModel> options;
    private OffsetDateTime createdAt;
    private OffsetDateTime expirationDate;
    private long totalVotes;

    public boolean hasExpired()
    {
        return expirationDate != null && OffsetDateTime.now().isAfter(expirationDate);
    }

    public boolean isOpen()
    {
        return !hasExpired();
    }

    public int optionCount()
    {
        return options == null ? 0 : options.size();
    }

    public boolean hasOptions()
    {
        return optionCount() > 0;
    }

    public void validateBasic()
    {
        if (pollId == null) throw new IllegalStateException("pollId is required");
        if (authorId == null) throw new IllegalStateException("authorId is required");
        if (question == null || question.isBlank()) throw new IllegalStateException("question is required");
        if (createdAt == null) throw new IllegalStateException("createdAt is required");
        if (expirationDate != null && expirationDate.isBefore(createdAt))
            throw new IllegalStateException("expirationDate cannot be before createdAt");
        if (options == null || options.size() < 2)
            throw new IllegalStateException("poll must have at least 2 options");
    }

    public PollOptionModel findOption(UUID optionId)
    {
        if (optionId == null || options == null) return null;
        for (PollOptionModel o : options)
        {
            if (o != null && optionId.equals(o.getOptionId())) return o;
        }
        return null;
    }
}


