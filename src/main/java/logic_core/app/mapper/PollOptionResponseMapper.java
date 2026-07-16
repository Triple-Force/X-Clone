package logic_core.app.mapper;

import logic_core.app.dto.response.PollOptionResponse;
import logic_core.domain.model.PollOptionModel;

import java.util.UUID;

public class PollOptionResponseMapper
{
    public PollOptionResponse toResponse(PollOptionModel model)
    {
        if (model == null)
            return null;

        return new PollOptionResponse(
                model.getOptionId(),
                model.getText(),
                model.getVoteCount()
        );
    }

    public PollOptionResponse toResponse(PollOptionModel model, UUID selectedOptionId)
    {
        if (model == null)
            return null;

        return new PollOptionResponse(
                model.getOptionId(),
                model.getText(),
                model.getVoteCount()
        );
    }
}