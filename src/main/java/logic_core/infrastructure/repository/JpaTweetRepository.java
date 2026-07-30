package logic_core.infrastructure.repository;

import Shared.Models.Tweet.Tweet;
import Shared.Models.TweetEdit.TweetEdit;
import jakarta.persistence.EntityManager;
import logic_core.app.dto.timeline.TimelineTweet;
import logic_core.domain.model.TweetModel;
import logic_core.domain.repository.TimelineType;
import logic_core.domain.repository.TweetRepository;
import logic_core.infrastructure.dao.TweetDao;
import logic_core.infrastructure.dao.TweetEditDao;
import logic_core.infrastructure.mapper.TweetPersistenceMapper;
import logic_core.infrastructure.projection.TimelineTweetProjection;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class JpaTweetRepository implements TweetRepository
{
    private final TweetDao tweetDao;
    private final TweetEditDao tweetEditDao;
    private final EntityManager entityManager;

    public JpaTweetRepository(TweetDao tweetDao, TweetEditDao tweetEditDao, EntityManager entityManager)
    {
        this.tweetDao = Objects.requireNonNull(tweetDao, "tweetDao must not be null");
        this.tweetEditDao = Objects.requireNonNull(tweetEditDao, "tweetEditDao must not be null");
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager must not be null");
    }

    @Override
    public Optional<TweetModel> save(TweetModel tweetModel)
    {
        Objects.requireNonNull(tweetModel, "tweetModel must not be null");
        Tweet entity = TweetPersistenceMapper.toPersistence(tweetModel, entityManager);
        Tweet saved = tweetDao.save(entity);
        return Optional.ofNullable(TweetPersistenceMapper.toModel(saved));
    }

    @Override
    public void update(TweetModel model)
    {
        Objects.requireNonNull(model, "tweetModel must not be null");
        if (model.getId() == null)
        {
            throw new IllegalArgumentException("tweet id is required for update");
        }

        tweetDao.findById(model.getId()).ifPresent(entity -> {
            TweetPersistenceMapper.updateEntity(entity, model, entityManager);
            tweetDao.updateTweet(entity);
        });
    }

    @Override
    public void softDelete(UUID tweetId)
    {
        Objects.requireNonNull(tweetId, "tweetId must not be null");

        tweetDao.findById(tweetId).ifPresent(entity -> {
            if (entity.isDeleted())
            {
                return;
            }
            entity.setDeleted(true);
            entity.redact();
            tweetDao.updateTweet(entity);
        });
    }

    @Override
    public Optional<TweetModel> findById(UUID tweetId)
    {
        return tweetDao.findById(tweetId).map(TweetPersistenceMapper::toModel);
    }

    @Override
    public Optional<TweetModel> findActiveById(UUID tweetId)
    {
        return tweetDao.findActiveById(tweetId).map(TweetPersistenceMapper::toModel);
    }

    @Override
    public List<TweetModel> findByAuthorId(UUID authorId)
    {
        return mapList(tweetDao.findByAuthorId(authorId));
    }

    @Override
    public List<TweetModel> findActiveByAuthorId(UUID authorId)
    {
        return mapList(tweetDao.findActiveByAuthorId(authorId));
    }

    @Override
    public List<TweetModel> findRepliesByTweetId(UUID tweetId)
    {
        return mapList(tweetDao.findRepliesByTweetId(tweetId));
    }

    @Override
    public List<TweetModel> findRetweetsOfTweet(UUID tweetId)
    {
        return mapList(tweetDao.findRetweetsOfTweet(tweetId));
    }

    @Override
    public List<TweetModel> findQuotesOfTweet(UUID tweetId)
    {
        return mapList(tweetDao.findQuotesOfTweet(tweetId));
    }

    @Override
    public List<TweetModel> findTweetsByAuthorId(UUID authorId)
    {
        return findActiveByAuthorId(authorId);
    }

    @Override
    public long countTweetsById(UUID authorId)
    {
        return findTweetsByAuthorId(authorId).size();
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
    public long countRepliesByTweetId(UUID tweetId)
    {
        return tweetDao.countRepliesByTweetId(tweetId);
    }

    @Override
    public long countRetweetsByTweetId(UUID tweetId)
    {
        return tweetDao.countRetweetsByTweetId(tweetId);
    }

    @Override
    public List<TweetModel> findTweetsRepliedByUser(UUID userId)
    {
        return mapList(tweetDao.findTweetsRepliedByUser(userId));
    }

    @Override
    public List<TweetModel> findTweetsRetweetedByUser(UUID userId)
    {
        return mapList(tweetDao.findTweetsRetweetedByUser(userId));
    }

    @Override
    public List<TweetModel> findTimelineTweets(UUID userId)
    {
        return mapList(tweetDao.findTimelineTweets(userId));
    }

    @Override
    public boolean existsById(UUID tweetId)
    {
        return tweetDao.existsById(tweetId);
    }

    @Override
    public boolean existsActiveById(UUID tweetId)
    {
        return tweetDao.existsActiveById(tweetId);
    }

    @Override
    public TweetEdit appendEditHistory(UUID tweetId, String previousContent)
    {
        Objects.requireNonNull(tweetId, "tweetId must not be null");
        Objects.requireNonNull(previousContent, "previousContent must not be null");

        Tweet tweet = tweetDao.findById(tweetId)
                .orElseThrow(() -> new IllegalArgumentException("Tweet not found."));

        TweetEdit edit = TweetEdit.builder()
                .tweet(tweet)
                .previousContent(previousContent)
                .build();

        return tweetEditDao.save(edit);
    }

    @Override
    public List<TweetEdit> findEditHistoryByTweetId(UUID tweetId)
    {
        Objects.requireNonNull(tweetId, "tweetId must not be null");
        return tweetEditDao.findHistoryByTweetId(tweetId);
    }

    private static List<TweetModel> mapList(List<Tweet> entities)
    {
        if (entities == null || entities.isEmpty())
        {
            return List.of();
        }
        return entities.stream()
                .map(TweetPersistenceMapper::toModel)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimelineTweet> getTimeline(
            TimelineType type,
            UUID actorId,
            UUID targetUserId,
            int limit,
            int offset)
    {
        List<TimelineTweetProjection> projections =
                tweetDao.getTimeline(
                        type,
                        actorId,
                        targetUserId,
                        limit,
                        offset
                );

        return projections.stream()
                .map(this::toTimelineTweet)
                .toList();
    }

    @Override
    public long countTimeline(
            TimelineType type,
            UUID actorId,
            UUID targetUserId)
    {
        return tweetDao.countTimeline(
                type,
                actorId,
                targetUserId
        );
    }


    private TimelineTweet toTimelineTweet(
            TimelineTweetProjection projection)
    {
        return TimelineTweet.builder()
                .tweetId(projection.tweetId())
                .authorId(projection.authorId())
                .username(projection.username())
                .displayName(projection.displayName())
                .avatarUrl(projection.avatarUrl())
                .content(projection.content())
                .likeCount(projection.likeCount())
                .replyCount(projection.replyCount())
                .retweetCount(projection.retweetCount())
                .publishedAt(projection.publishedAt())
                .build();
    }

    @Override
    public Optional<TweetModel> findActiveByIdForUpdate(UUID tweetId)
    {
        return tweetDao.findActiveByIdForUpdate(tweetId)
                .map(TweetPersistenceMapper::toModel);
    }
}