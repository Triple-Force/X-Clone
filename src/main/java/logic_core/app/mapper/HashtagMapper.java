package logic_core.app.mapper;

import logic_core.app.dto.response.HashtagResponse;
import logic_core.app.dto.response.HashtagTrendResponse;
import logic_core.domain.model.HashtagModel;

public final class HashtagMapper
{
    private HashtagMapper()
    {

    }

    public static HashtagResponse toResponse(HashtagModel hashtag)
    {
        return new HashtagResponse(
                hashtag.getHashtagId(),
                hashtag.getTag()
        );
    }
}
