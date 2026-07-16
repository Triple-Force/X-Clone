package logic_core.app.mapper;

import logic_core.app.dto.response.PollResponse;
import logic_core.domain.model.PollModel;

public class PollResponseMapper
{
    public PollResponse toResponse(PollModel poll, boolean expired)
    {
        if (poll == null)
        {
            return null;
        }

        return new PollResponse(
                poll.getPollId(),
                poll.getTweet().getId(),
                poll.getAuthorId(),
                poll.getQuestion(),
                poll.getOptions(),
                poll.getCreatedAt(),
                poll.getExpirationDate(),
                poll.getTotalVotes(),
                expired
        );
    }
}
