package logic_core.infrastructure.dao;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Tweet.Tweet;
import jakarta.persistence.LockModeType;
import logic_core.domain.repository.TimelineType;
import logic_core.infrastructure.projection.TimelineTweetProjection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TweetDao extends GenericDAO<Tweet> {

    private static final String BASE_SELECT = """
    SELECT new logic_core.infrastructure.projection.TimelineTweetProjection(
    t.id,
    a.id,
    a.username,
    a.displayName,
    a.avatarUrl,
    t.content,
    COUNT(DISTINCT l.user),
    COUNT(DISTINCT r),
    COUNT(DISTINCT rt),
    t.publishedAt
    )
    """;

    private static final String BASE_FROM = """
    FROM Tweet t
    JOIN t.author a
    LEFT JOIN t.likes l
    LEFT JOIN t.replies r
    LEFT JOIN t.retweets rt
    """;

    private static final String ACTIVE_TWEET_FILTER = """
    t.isDeleted = false
    """;

    private static final String BLOCK_FILTER = """
    NOT EXISTS (
    SELECT 1
    FROM Block b
    WHERE
    (
        b.blocker.id = :actorId
        AND
        b.blocked.id = a.id
    )
    OR
    (
        b.blocker.id = a.id
        AND
        b.blocked.id = :actorId
    )
    )
    """;


    private static final String MUTE_FILTER = """
    a.id NOT IN (
    SELECT m.muted.id
    FROM Mute m
    WHERE m.muter.id = :actorId
    )
    """;


    private static final String GROUP_BY = """
    GROUP BY
    t.id,
    a.id,
    a.username,
    a.displayName,
    a.avatarUrl,
    t.content,
    t.publishedAt
    """;


    private static final String ORDER_BY = """
    ORDER BY
    t.publishedAt DESC
    """;

    private static final String PAGE_ORDER = """
    ORDER BY t.publishedAt DESC
    """;

    public TweetDao()
    {
        super(Tweet.class);
    }

    public Tweet save(Tweet tweet)
    {
        insert(tweet);
        return tweet;
    }

    public void updateTweet(Tweet tweet)
    {
        update(tweet);
    }

    public Optional<Tweet> findById(UUID tweetId)
    {
        return Optional.ofNullable(
                findOneByJpql(
                        "SELECT t FROM Tweet t WHERE t.id = :id",
                        q -> q.setParameter("id", tweetId)
                )
        );
    }

    public Optional<Tweet> findActiveById(UUID tweetId)
    {
        return Optional.ofNullable(
                findOneByJpql(
                        "SELECT t FROM Tweet t WHERE t.id = :id AND t.isDeleted = false",
                        q -> q.setParameter("id", tweetId)
                )
        );
    }

    public boolean existsById(UUID tweetId)
    {
        long count = countByJpql(
                "SELECT COUNT(t) FROM Tweet t WHERE t.id = :id AND t.isDeleted = false",
                q -> q.setParameter("id", tweetId)
        );
        return count > 0;
    }

    public boolean existsActiveById(UUID tweetId)
    {
        long count = countByJpql(
                "SELECT COUNT(t) FROM Tweet t WHERE t.id = :id AND t.isDeleted = false",
                q -> q.setParameter("id", tweetId)
        );
        return count > 0;
    }

    public Optional<Tweet> findActiveByIdForUpdate(UUID tweetId)
    {
        return Optional.ofNullable(
                findOneByJpql(
                        "SELECT t FROM Tweet t WHERE t.id = :id AND t.isDeleted = false",
                        q -> q.setParameter("id", tweetId)
                                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                )
        );
    }

    public Boolean isRetweetedByUser(UUID tweetId, UUID userId)
    {
        long count = countByJpql(
                "SELECT COUNT(t) FROM Tweet t " +
                        "WHERE t.retweetedTweet.id = :tweetId " +
                        "AND t.author.id = :userId " +
                        "AND t.isDeleted = false",
                q -> q.setParameter("tweetId", tweetId)
                        .setParameter("userId", userId)
        );
        return count > 0;
    }

    public Boolean isRepliedByUser(UUID tweetId, UUID userId)
    {
        long count = countByJpql(
                "SELECT COUNT(t) FROM Tweet t " +
                        "WHERE t.repliedToTweet.id = :tweetId " +
                        "AND t.author.id = :userId " +
                        "AND t.isDeleted = false",
                q -> q.setParameter("tweetId", tweetId)
                        .setParameter("userId", userId)
        );
        return count > 0;
    }

    public List<Tweet> findByAuthorId(UUID authorId)
    {
        return findByJpql(
                "SELECT t FROM Tweet t " +
                        "WHERE t.author.id = :authorId " +
                        "AND t.isDeleted = false " +
                        "ORDER BY t.createdAt DESC",
                q -> q.setParameter("authorId", authorId)
        );
    }

    public List<Tweet> findActiveByAuthorId(UUID authorId)
    {
        return findByJpql(
                "SELECT t FROM Tweet t " +
                        "WHERE t.author.id = :authorId AND t.isDeleted = false " +
                        "ORDER BY t.createdAt DESC",
                q -> q.setParameter("authorId", authorId)
        );
    }

// ── thread / graph (فعال) ─────────────────────────────────────────

    public List<Tweet> findRepliesByTweetId(UUID parentTweetId)
    {
        return findByJpql(
                "SELECT t FROM Tweet t " +
                        "WHERE t.repliedToTweet.id = :parentTweetId AND t.isDeleted = false " +
                        "ORDER BY t.createdAt ASC",
                q -> q.setParameter("parentTweetId", parentTweetId)
        );
    }

    public List<Tweet> findRetweetsOfTweet(UUID originalTweetId)
    {
        return findByJpql(
                "SELECT t FROM Tweet t " +
                        "WHERE t.retweetedTweet.id = :originalTweetId AND t.isDeleted = false " +
                        "ORDER BY t.createdAt DESC",
                q -> q.setParameter("originalTweetId", originalTweetId)
        );
    }

    public List<Tweet> findQuotesOfTweet(UUID originalTweetId)
    {
        return findByJpql(
                "SELECT t FROM Tweet t " +
                        "WHERE t.quotedTweet.id = :originalTweetId AND t.isDeleted = false " +
                        "ORDER BY t.createdAt DESC",
                q -> q.setParameter("originalTweetId", originalTweetId)
        );
    }

    public long countRepliesByTweetId(UUID tweetId)
    {
        return countByJpql(
                "SELECT COUNT(t) FROM Tweet t " +
                        "WHERE t.repliedToTweet.id = :tweetId AND t.isDeleted = false",
                q -> q.setParameter("tweetId", tweetId)
        );
    }

    public long countRetweetsByTweetId(UUID tweetId)
    {
        return countByJpql(
                "SELECT COUNT(t) FROM Tweet t " +
                        "WHERE t.retweetedTweet.id = :tweetId AND t.isDeleted = false",
                q -> q.setParameter("tweetId", tweetId)
        );
    }

    // ── timeline / hashtag ────────────────────────────────────────────

    public List<Tweet> findTimelineTweets(UUID userId)
    {
        return findByJpql(
                "SELECT t FROM Tweet t " +
                        "WHERE t.isDeleted = false AND (" +
                        "t.author.id = :userId OR t.author.id IN (" +
                        "SELECT f.following.id FROM Follow f " +
                        "WHERE f.follower.id = :userId " +
                        "AND f.isDeleted = false" +
                        ")" +
                        ") ORDER BY t.createdAt DESC",
                q -> q.setParameter("userId", userId)
        );
    }

    public List<Tweet> findTweetsByHashtag(UUID hashtagId)
    {
        return findByJpql(
                "SELECT th.tweet FROM TweetHashtag th " +
                        "WHERE th.hashtag.id = :hashtagId AND th.tweet.isDeleted = false " +
                        "ORDER BY th.tweet.createdAt DESC",
                q -> q.setParameter("hashtagId", hashtagId)
        );
    }

    // ── user activity sets ────────────────────────────────────────────

    public List<Tweet> findTweetsRepliedByUser(UUID userId) {
        return findByJpql(
                "SELECT DISTINCT t.repliedToTweet FROM Tweet t " +
                        "WHERE t.author.id = :userId " +
                        "AND t.repliedToTweet IS NOT NULL " +
                        "AND t.isDeleted = false " +
                        "AND t.repliedToTweet.isDeleted = false " +
                        "ORDER BY t.repliedToTweet.createdAt DESC",
                q -> q.setParameter("userId", userId)
        );
    }

    public List<Tweet> findTweetsRetweetedByUser(UUID userId) {
        return findByJpql(
                "SELECT DISTINCT t.retweetedTweet FROM Tweet t " +
                        "WHERE t.author.id = :userId " +
                        "AND t.retweetedTweet IS NOT NULL " +
                        "AND t.isDeleted = false " +
                        "AND t.retweetedTweet.isDeleted = false " +
                        "ORDER BY t.retweetedTweet.createdAt DESC",
                q -> q.setParameter("userId", userId)
        );
    }

    public List<TimelineTweetProjection> getTimeline(
            TimelineType type,
            UUID actorId,
            UUID targetUserId,
            int limit,
            int offset)
    {
        return switch (type)
        {
            case HOME ->
                    loadHomeTimeline(actorId, limit, offset);

            case FOLLOWING ->
                    loadFollowingTimeline(actorId, limit, offset);

            case USER ->
                    loadUserTimeline(actorId, targetUserId, limit, offset);

            case REPLIES ->
                    loadRepliesTimeline(actorId, targetUserId, limit, offset);

            case MEDIA ->
                    loadMediaTimeline(actorId, targetUserId, limit, offset);

            case LIKED ->
                    loadLikedTimeline(actorId, targetUserId, limit, offset);
        };
    }


    public long countTimeline(
            TimelineType type,
            UUID actorId,
            UUID targetUserId)
    {
        return switch (type)
        {
            case HOME ->
                    countHomeTimeline(actorId);

            case FOLLOWING ->
                    countFollowingTimeline(actorId);

            case USER ->
                    countUserTimeline(actorId, targetUserId);

            case REPLIES ->
                    countRepliesTimeline(actorId, targetUserId);

            case MEDIA ->
                    countMediaTimeline(actorId, targetUserId);

            case LIKED ->
                    countLikedTimeline(actorId, targetUserId);
        };
    }


    private String buildTimelineQuery(
            String specificWhere,
            boolean applyMute)
    {
        return BASE_SELECT +
                BASE_FROM +
                buildTimelineWhereClause(
                        specificWhere,
                        applyMute
                ) +
                GROUP_BY +
                ORDER_BY;
    }

    private String buildTimelineCountQuery(
            String specificWhere,
            boolean applyMute)
    {
        return """
            SELECT COUNT(t)
            FROM Tweet t
            JOIN t.author a
            """
                +
                buildTimelineWhereClause(
                        specificWhere,
                        applyMute
                );
    }


    private String buildTimelineWhereClause(
            String specificWhere,
            boolean applyMute)
    {
        StringBuilder where = new StringBuilder();

        where.append("""
            WHERE
            """)
                .append(ACTIVE_TWEET_FILTER)
                .append("""

                    AND
                    """)
                .append(specificWhere)
                .append("""

                    AND
                    """)
                .append(BLOCK_FILTER);

        if (applyMute)
        {
            where.append("""

                    AND
                    """)
                    .append(MUTE_FILTER);
        }

        return where.toString();
    }

    public List<TimelineTweetProjection> loadHomeTimeline(
            UUID actorId,
            int limit,
            int offset)
    {
        String jpql = buildTimelineQuery(
                """
                (
                    a.id = :actorId
    
                    OR
    
                    a.id IN
                    (
                        SELECT f.following.id
                        FROM Follow f
                        WHERE f.follower.id = :actorId
                    )
                )
                """,
                true
        );

        return findProjectionByJpql(
                jpql,
                TimelineTweetProjection.class,
                query ->
                {
                    query.setParameter("actorId", actorId);
                },
                limit,
                offset
        );
    }

    public List<TimelineTweetProjection> loadFollowingTimeline(
            UUID actorId,
            int limit,
            int offset)
    {
        String jpql = buildTimelineQuery(
                """
                a.id IN
                (
                    SELECT f.following.id
                    FROM Follow f
                    WHERE f.follower.id = :actorId
                )
                """,
                true
        );

        return findProjectionByJpql(
                jpql,
                TimelineTweetProjection.class,
                query ->
                {
                    query.setParameter("actorId", actorId);
                },
                limit,
                offset
        );
    }

    public List<TimelineTweetProjection> loadUserTimeline(
            UUID actorId,
            UUID targetUserId,
            int limit,
            int offset)
    {
        String jpql = buildTimelineQuery(
                """
                a.id = :targetUserId
                """,
                false
        );

        return findProjectionByJpql(
                jpql,
                TimelineTweetProjection.class,
                query ->
                {
                    query.setParameter("actorId", actorId);
                    query.setParameter("targetUserId", targetUserId);
                },
                limit,
                offset
        );
    }

    public List<TimelineTweetProjection> loadRepliesTimeline(
            UUID actorId,
            UUID targetUserId,
            int limit,
            int offset)
    {
        String jpql = buildTimelineQuery(
                """
                a.id = :targetUserId
    
                AND
    
                t.repliedToTweet IS NOT NULL
                """,
                false
        );

        return findProjectionByJpql(
                jpql,
                TimelineTweetProjection.class,
                query ->
                {
                    query.setParameter("actorId", actorId);
                    query.setParameter("targetUserId", targetUserId);
                },
                limit,
                offset
        );
    }

    public List<TimelineTweetProjection> loadMediaTimeline(
            UUID actorId,
            UUID targetUserId,
            int limit,
            int offset)
    {
        String jpql = buildTimelineQuery(
                """
                a.id = :targetUserId
    
                AND
    
                EXISTS
                (
                    SELECT 1
                    FROM Media m
                    WHERE m.tweet = t
                )
                """,
                false
        );

        return findProjectionByJpql(
                jpql,
                TimelineTweetProjection.class,
                query ->
                {
                    query.setParameter("actorId", actorId);
                    query.setParameter("targetUserId", targetUserId);
                },
                limit,
                offset
        );
    }


    public List<TimelineTweetProjection> loadLikedTimeline(
            UUID actorId,
            UUID targetUserId,
            int limit,
            int offset)
    {
        String jpql =
                BASE_SELECT +
                        BASE_FROM +
                        """
                        JOIN t.likes liked
    
                        WHERE
                        """ +
                        ACTIVE_TWEET_FILTER +
                        """
    
                        AND
    
                        liked.user.id = :targetUserId
    
                        AND
                        """ +
                        BLOCK_FILTER +
                        GROUP_BY +
                        ORDER_BY;

        return findProjectionByJpql(
                jpql,
                TimelineTweetProjection.class,
                query ->
                {
                    query.setParameter("actorId", actorId);
                    query.setParameter("targetUserId", targetUserId);
                },
                limit,
                offset
        );
    }


    public long countHomeTimeline(UUID actorId)
    {
        String jpql = buildTimelineCountQuery(
                """
                (
                    a.id = :actorId
    
                    OR
    
                    a.id IN
                    (
                        SELECT f.following.id
                        FROM Follow f
                        WHERE f.follower.id = :actorId
                    )
                )
                """,
                true
        );

        return countByJpql(
                jpql,
                query ->
                        query.setParameter("actorId", actorId)
        );
    }

    public long countFollowingTimeline(UUID actorId)
    {
        String jpql = buildTimelineCountQuery(
                """
                a.id IN
                (
                    SELECT f.following.id
                    FROM Follow f
                    WHERE f.follower.id = :actorId
                )
                """,
                true
        );

        return countByJpql(
                jpql,
                query ->
                        query.setParameter("actorId", actorId)
        );
    }

    public long countUserTimeline(
            UUID actorId,
            UUID targetUserId)
    {
        String jpql = buildTimelineCountQuery(
                """
                a.id = :targetUserId
                """,
                false
        );

        return countByJpql(
                jpql,
                query ->
                {
                    query.setParameter("actorId", actorId);
                    query.setParameter("targetUserId", targetUserId);
                }
        );
    }

    public long countRepliesTimeline(
            UUID actorId,
            UUID targetUserId)
    {
        String jpql = buildTimelineCountQuery(
                """
                a.id = :targetUserId
    
                AND
    
                t.repliedToTweet IS NOT NULL
                """,
                false
        );

        return countByJpql(
                jpql,
                query ->
                {
                    query.setParameter("actorId", actorId);
                    query.setParameter("targetUserId", targetUserId);
                }
        );
    }

    public long countMediaTimeline(
            UUID actorId,
            UUID targetUserId)
    {
        String jpql = buildTimelineCountQuery(
                """
                a.id = :targetUserId
    
                AND
    
                EXISTS
                (
                    SELECT 1
                    FROM Media m
                    WHERE m.tweet = t
                )
                """,
                false
        );

        return countByJpql(
                jpql,
                query ->
                {
                    query.setParameter("actorId", actorId);
                    query.setParameter("targetUserId", targetUserId);
                }
        );
    }

    public long countLikedTimeline(
            UUID actorId,
            UUID targetUserId)
    {
        String jpql = """
        SELECT COUNT(DISTINCT t)
        FROM Tweet t
        JOIN t.author a
        JOIN t.likes liked
        """
                +
                buildTimelineWhereClause(
                        """
                        liked.user.id = :targetUserId
                        """,
                        false
                );

        return countByJpql(
                jpql,
                query ->
                {
                    query.setParameter("actorId", actorId);
                    query.setParameter("targetUserId", targetUserId);
                }
        );
    }
}
