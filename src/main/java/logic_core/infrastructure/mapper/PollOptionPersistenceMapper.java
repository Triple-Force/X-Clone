package logic_core.infrastructure.mapper;

import Shared.Models.Poll.Poll;
import Shared.Models.PollOption.PollOption;
import logic_core.domain.model.PollOptionModel;

import java.util.ArrayList;

public class PollOptionPersistenceMapper
{

    public PollOption toPersistence(PollOptionModel domain)
    {
        if (domain == null)
            return null;

        Poll poll = new Poll();


        PollOption entity = new PollOption();
        entity.setPoll(poll);
        entity.setOptionText(domain.getText());
        entity.setVotes(new ArrayList<>());

        return entity;
    }

    public PollOptionModel toDomain(PollOption entity)
    {
        if (entity == null)
            return null;

        long voteCount = entity.getVotes() == null ? 0L : entity.getVotes().size();

        return PollOptionModel.builder()
                .optionId(entity.getId())
                .pollId(entity.getPoll() != null ? entity.getPoll().getId() : null)
                .text(entity.getOptionText())
                .voteCount(voteCount)
                .build();
    }
}
