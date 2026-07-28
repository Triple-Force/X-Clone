package logic_core.app.mapper;

import logic_core.app.dto.response.TweetResponse;
import logic_core.app.dto.response.UserSummaryResponse;
import logic_core.domain.model.TweetModel;

import java.util.UUID;

public class TweetMapper
{
    private TweetMapper() {}

    public static TweetResponse toResponse(
            TweetModel model,
            UserSummaryResponse author,
            TweetResponse repliedTweet,
            TweetResponse retweetedTweet,
            TweetResponse quotedTweet
    ) {
        if (model == null) return null;

        return new TweetResponse(
                model.getId(),
                model.getContent(),
                author,
                model.isDeleted(),
                model.isPinned(),
                model.getCreatedAt(),
                model.getPublishedAt(),
                model.getRepliedToTweetId(),
                retweetedTweet,
                quotedTweet,
                model.getLikeCount(),
                model.getReplyCount(),
                model.getRetweetCount()
        );
    }

    public static TweetModel toModel(
            UUID replyToId,
            UUID quoteOfId,
            String content,
            UUID authorId
    )
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
