package logic_core.infrastructure.repository;

import logic_core.domain.model.TweetModel;
import logic_core.domain.repository.TweetRepository;
import logic_core.infrastructure.dao.TweetDao;
import logic_core.infrastructure.mapper.TweetPersistenceMapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class JpaTweetRepository implements TweetRepository
{
    private final TweetDao tweetDao;

    public JpaTweetRepository(TweetDao tweetDao)
    {
        this.tweetDao = Objects.requireNonNull(tweetDao, "tweetDa most not be null");
    }

    @Override
    public Optional<TweetModel> findById(UUID tweetId)
    {
        return tweetDao.findById(tweetId)
                .map(TweetPersistenceMapper::toModel);
    }

    @Override
    public List<TweetModel> findTweetsByAuthorId(UUID authorId)
    {
        return tweetDao.findByAuthorId(authorId).stream()
                .map(TweetPersistenceMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isRepliedByUser(UUID tweetId, UUID userId)
    {
        Boolean result = tweetDao.isRepliedByUser(tweetId, userId);
        return result != null && result;
    }

    @Override
    public boolean isRetweetedByUser(UUID tweetId, UUID userId)
    {
        Boolean result = tweetDao.isRetweetedByUser(tweetId, userId);
        return result != null && result;
    }

    @Override
    public List<TweetModel> findTweetsRepliedByUser(UUID userId)
    {
        return tweetDao.findTweetsRepliedByUser(userId).stream()
                .map(TweetPersistenceMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<TweetModel> findTweetsRetweetedByUser(UUID userId)
    {
        return tweetDao.findTweetsRetweetedByUser(userId).stream()
                .map(TweetPersistenceMapper::toModel)
                .collect(Collectors.toList());
    }
}