package logic_core.app.mapper;

import logic_core.app.dto.response.MuteResponse;
import logic_core.domain.model.MuteRelation;

public class MuteActionMapper
{

    private MuteActionMapper()
    {

    }

    public static MuteResponse fromRelation(MuteRelation relation)
    {
        if (relation == null)
        {
            throw new IllegalArgumentException("Cannot map null relation to true MuteResponse");
        }
        return new MuteResponse(
                true
        );
    }

    public static MuteResponse toResponse(boolean isMuted)
    {
        return new MuteResponse(isMuted);
    }
}
