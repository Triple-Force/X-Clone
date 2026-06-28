package service;

import enums.TimelineItemType;
import lombok.RequiredArgsConstructor;
import model.Retweet;
import model.TimelineItem;
import model.Tweet;
import repository.LikeRepository;
import repository.RetweetRepository;
import repository.TweetRepository;

import java.util.*;

@RequiredArgsConstructor
public class TimelineService
{
    private final TweetRepository tweetRepository;
    private final RetweetRepository retweetRepository;
    private final LikeRepository likeRepository;

    public List<TimelineItem> getTimeline(Long currentUserId, Set<Long> followingIds)
    {
        List<TimelineItem> timeline = new ArrayList<>();

        Set<Long> allTargetIds = new HashSet<>(followingIds);
        allTargetIds.add(currentUserId);

        List<Tweet> tweets = tweetRepository.findFollowingTweets(allTargetIds);
        List<Retweet> retweets = retweetRepository.findByUserIds(allTargetIds);


        Set<Long> neededTweetIds = new HashSet<>();

        for (Tweet tweet : tweets)
        {
            if (tweet.getQuotedTweetId() != null)
            {
                neededTweetIds.add(tweet.getQuotedTweetId());
            }
        }

        for (Retweet retweet : retweets)
        {
            neededTweetIds.add(retweet.getOriginalTweetId());
        }

        Map<Long, Tweet> tweetMap = loadTweetsByIds(neededTweetIds);

        for (Tweet tweet : tweets)
        {
            if (tweet.getQuotedTweetId() == null)
            {
                timeline.add(toNormalTweetItem(tweet, currentUserId));
            }
            else
            {
                Tweet original = tweetMap.get(tweet.getQuotedTweetId());

                timeline.add(
                        toQuoteTweetItem(tweet, original, currentUserId)
                );
            }
        }

        for (Retweet retweet : retweets)
        {
            Tweet original = tweetMap.get(retweet.getOriginalTweetId());

            if (original != null)
            {
                timeline.add(
                        toSimpleRetweetItem(retweet, original, currentUserId)
                );
            }
        }

        timeline.sort(
                Comparator.comparing(TimelineItem::getCreatedAt).reversed()
        );

        return timeline;
    }


    private void addTweetsToTimeline(List<TimelineItem> timeline,
                                     Long currentUserId,
                                     Set<Long> followingIds,
                                     Map<Long, Tweet> tweetMap)
    {

        List<Tweet> tweets = tweetRepository.findFollowingTweets(followingIds);

        for (Tweet tweet : tweets)
        {
            if (isQuoteTweet(tweet))
            {
                Tweet originalTweet = tweetMap.get(tweet.getQuotedTweetId());
                timeline.add(toQuoteTweetItem(tweet, originalTweet, currentUserId));
            }
            else
            {
                timeline.add(toNormalTweetItem(tweet, currentUserId));
            }
        }
    }


    private void addRetweetsToTimeline(List<TimelineItem> timeline,
                                       Long currentUserId,
                                       Set<Long> followingIds)
    {

        List<Retweet> retweets = retweetRepository.findByUserIds(followingIds);

        for (Retweet retweet : retweets)
        {
            tweetRepository.findById(retweet.getOriginalTweetId())
                    .ifPresent(originalTweet ->
                            timeline.add(
                                    toSimpleRetweetItem(retweet, originalTweet, currentUserId)
                            )
                    );
        }
    }

    private boolean isQuoteTweet(Tweet tweet)
    {
        return tweet.getQuotedTweetId() != null;
    }

    private TimelineItem toNormalTweetItem(Tweet tweet, Long currentUserId)
    {
        return TimelineItem.builder()
                .type(TimelineItemType.NORMAL_TWEET)
                .tweet(tweet)
                .originalTweet(null)
                .actorUserId(tweet.getAuthorId())
                .createdAt(tweet.getCreatedAt())
                .likedByCurrentUser(isLikedByCurrentUser(tweet.getId(), currentUserId))
                .retweetedByCurrentUser(isRetweetedByCurrentUser(tweet.getId(), currentUserId))
                .likeCount(likeRepository.countByTweetId(tweet.getId()))
                .build();
    }

    private TimelineItem toQuoteTweetItem(Tweet quoteTweet, Tweet originalTweet, Long currentUserId)
    {
        return TimelineItem.builder()
                .type(TimelineItemType.QUOTE_TWEET)
                .tweet(quoteTweet)
                .originalTweet(originalTweet)
                .actorUserId(quoteTweet.getAuthorId())
                .createdAt(quoteTweet.getCreatedAt())
                .likedByCurrentUser(isLikedByCurrentUser(quoteTweet.getId(), currentUserId))
                .retweetedByCurrentUser(isRetweetedByCurrentUser(quoteTweet.getId(), currentUserId))
                .likeCount(likeRepository.countByTweetId(quoteTweet.getId()))
                .build();
    }

    private TimelineItem toSimpleRetweetItem(Retweet retweet, Tweet originalTweet, Long currentUserId)
    {
        return TimelineItem.builder()
                .type(TimelineItemType.SIMPLE_RETWEET)
                .tweet(originalTweet)
                .originalTweet(null)
                .actorUserId(retweet.getUserId())
                .createdAt(retweet.getCreatedAt())
                .likedByCurrentUser(isLikedByCurrentUser(originalTweet.getId(), currentUserId))
                .retweetedByCurrentUser(isRetweetedByCurrentUser(originalTweet.getId(), currentUserId))
                .likeCount(likeRepository.countByTweetId(originalTweet.getId()))
                .build();
    }


    private boolean isLikedByCurrentUser(Long tweetId, Long currentUserId)
    {
        return likeRepository.isLikedByUser(currentUserId, tweetId);
    }

    private boolean isRetweetedByCurrentUser(Long tweetId, Long currentUserId)
    {
        return retweetRepository.existsByOriginalTweetIdAndUserId(tweetId, currentUserId);
    }

    private Map<Long, Tweet> loadTweetsByIds(Set<Long> ids)
    {
        List<Tweet> tweets = tweetRepository.findByIds(ids);

        Map<Long, Tweet> map = new HashMap<>();

        for (Tweet tweet : tweets)
        {
            map.put(tweet.getId(), tweet);
        }

        return map;
    }
}
