package logic_core.domain.repository;

import logic_core.domain.model.TweetModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TweetRepository
{
    Optional<TweetModel> findById(UUID TweetId);

    List<TweetModel> findTweetsByAuthorId(UUID authorId);

    boolean isRepliedByUser(UUID TweetId, UUID userId);

    boolean isRetweetedByUser(UUID tweetId, UUID userId);

    List<TweetModel> findTweetsRepliedByUser(UUID userId);

    List<TweetModel> findTweetsRetweetedByUser(UUID userId);
}
