package logic_core.domain.repository;

import Shared.Models.TweetEdit.TweetEdit;
import logic_core.app.dto.timeline.TimelineTweet;
import logic_core.domain.model.TweetModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TweetRepository
{
    // -------------------------------------------------------------------------
    // Write
    // -------------------------------------------------------------------------

    /**
     * Persist a new tweet (original / reply / retweet / quote).
     * Implementation maps TweetModel -> entity, then DAO save.
     *
     * @return saved model (with generated/confirmed id and timestamps if any)
     */
    Optional<TweetModel> save(TweetModel tweet);

    /**
     * Update mutable fields of an existing tweet (content edit, pin, counters if domain-owned).
     * Must not create a new row; missing tweet should surface as NotFound at use-case/policy layer
     * or via Optional-empty depending on your infra convention.
     */
    void update(TweetModel tweet);

    /**
     * Soft-delete tweet by id (sets deleted flag + entity redact/onSoftDelete cascade).
     * Hard delete is intentionally not exposed.
     */
    void softDelete(UUID tweetId);

    // -------------------------------------------------------------------------
    // Read – single
    // -------------------------------------------------------------------------

    /**
     * Load by id including soft-deleted tweets (needed for ownership/delete checks).
     */
    Optional<TweetModel> findById(UUID tweetId);

    /**
     * Load by id only if not soft-deleted.
     * Preferred for view / like / reply / retweet entry points.
     */
    Optional<TweetModel> findActiveById(UUID tweetId);

    // -------------------------------------------------------------------------
    // Read – collections
    // -------------------------------------------------------------------------

    /**
     * All tweets of author (including deleted if present in DAO result).
     * Prefer filtering deleted in DAO with is_deleted = false for public profile.
     */
    List<TweetModel> findByAuthorId(UUID authorId);

    /**
     * Active (non-deleted) tweets of author, typically ordered by createdAt DESC.
     */
    List<TweetModel> findActiveByAuthorId(UUID authorId);

    /**
     * Direct replies to a parent tweet (active only recommended).
     */
    List<TweetModel> findRepliesByTweetId(UUID tweetId);

    /**
     * Retweet rows that point to original tweetId.
     */
    List<TweetModel> findRetweetsOfTweet(UUID tweetId);

    /**
     * Quote tweets that quote tweetId.
     */
    List<TweetModel> findQuotesOfTweet(UUID tweetId);

    /**
     * Original tweets this user has replied to.
     */
    List<TweetModel> findTweetsRepliedByUser(UUID userId);

    /**
     * Original tweets this user has retweeted.
     */
    List<TweetModel> findTweetsRetweetedByUser(UUID userId);

    /**
     * Home/timeline candidates for user (self + following). Filtering blocks/mutes stays in use-case.
     */
    List<TweetModel> findTimelineTweets(UUID userId);

    // -------------------------------------------------------------------------
    // Existence / state (policy-friendly)
    // -------------------------------------------------------------------------

    boolean existsById(UUID tweetId);

    /**
     * true iff tweet exists and is not soft-deleted.
     */
    boolean existsActiveById(UUID tweetId);

    /**
     * Whether user already created a reply tweet under parentTweetId.
     * (Only if product forbids multiple replies; otherwise keep for analytics/UI.)
     */
    boolean isRepliedByUser(UUID tweetId, UUID userId);

    /**
     * Whether user already retweeted tweetId (non-deleted retweet row recommended).
     */
    boolean isRetweetedByUser(UUID tweetId, UUID userId);

    // -------------------------------------------------------------------------
    // Counts (optional but useful for TweetModel counter sync / responses)
    // -------------------------------------------------------------------------

    long countRepliesByTweetId(UUID tweetId);

    long countRetweetsByTweetId(UUID tweetId);

    List<TweetModel> findTweetsByAuthorId(UUID authorId);

    TweetEdit appendEditHistory(UUID tweetId, String previousContent);

    List<TweetEdit> findEditHistoryByTweetId(UUID tweetId);

    List<TimelineTweet> getTimeline(TimelineType type, UUID actorId, UUID targetUserId, int limit, int offset);

    long countTimeline(TimelineType type, UUID actorId, UUID targetUserId);

    Optional<TweetModel> findActiveByIdForUpdate(UUID tweetId);
}
