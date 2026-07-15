package logic_core.app.mapper;

import logic_core.domain.model.TweetModel;
import java.util.UUID;

public class TweetRequestMapper
{
    public static TweetModel toModel(UUID replyToId, UUID quoteOfId, String content, UUID authorId)
    {

        return TweetModel.builder()
                .authorId(authorId)
                .content(content)
                .repliedToTweetId(replyToId)
                .quotedTweetId(quoteOfId)
                .deleted(false)
                .pinned(false)
                .build();
    }
}
