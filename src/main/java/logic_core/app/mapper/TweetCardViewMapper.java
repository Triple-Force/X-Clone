package logic_core.app.mapper;

import logic_core.app.dto.view.TweetCardView;
import logic_core.app.dto.view.UserCardView;
import logic_core.domain.model.TweetModel;

public final class TweetCardViewMapper
{

    private TweetCardViewMapper() {}

    public static TweetCardView toView(
            TweetModel model,
            UserCardView author,
            boolean liked,
            boolean retweeted
    )
    {
        if (model == null) return null;

        return new TweetCardView(
                model.getId(),
                author,
                model.getContent(),
                model.getCreatedAt(),
                safeInt(model.getLikeCount()),
                safeInt(model.getReplyCount()),
                safeInt(model.getRetweetCount()),
                liked,
                retweeted,
                model.isPinned()
        );
    }

    private static int safeInt(long value)
    {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }
}
