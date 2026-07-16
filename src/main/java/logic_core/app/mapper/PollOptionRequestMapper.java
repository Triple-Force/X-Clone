package logic_core.app.mapper;

import logic_core.app.dto.request.PollOptionRequest;
import logic_core.domain.model.PollOptionModel;

public class PollOptionRequestMapper
{
    public PollOptionModel toDomain(PollOptionRequest dto)
    {
        if (dto == null)
            return null;

        return PollOptionModel.builder()
                .optionId(dto.optionId())
                .pollId(dto.pollId())
                .text(dto.text())
                .voteCount(0L)
                .build();
    }
}