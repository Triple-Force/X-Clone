package service;

import model.Retweet;
import repository.RetweetRepository;
import repository.TweetRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class RetweetService
{

    private final RetweetRepository retweetRepository;
    private final TweetRepository tweetRepository;

    public RetweetService(RetweetRepository retweetRepository,
                          TweetRepository tweetRepository)
    {
        this.retweetRepository = retweetRepository;
        this.tweetRepository = tweetRepository;
    }

    // ----------------------
    // Retweet
    // ----------------------

    public Retweet retweet(Long userId, Long originalTweetId)
    {
        // tweet must exist
        tweetRepository.findById(originalTweetId)
                .orElseThrow(() -> new RuntimeException("Tweet not found"));

        // prevent duplicate retweet
        if (retweetRepository.existsByOriginalTweetIdAndUserId(originalTweetId, userId))
        {
            throw new RuntimeException("User already retweeted this tweet");
        }

        Retweet retweet = Retweet.builder()
                .userId(userId)
                .originalTweetId(originalTweetId)
                .createdAt(LocalDateTime.now())
                .build();

        return retweetRepository.save(retweet);
    }

    // ----------------------
    // Undo retweet
    // ----------------------

    public void unretweet(Long userId, Long originalTweetId)
    {
        if (!retweetRepository.existsByOriginalTweetIdAndUserId(originalTweetId, userId))
        {
            throw new RuntimeException("Retweet does not exist");
        }

        retweetRepository.deleteByOriginalTweetIdAndUserId(originalTweetId, userId);
    }

    // ----------------------
    // Check retweet
    // ----------------------

    public boolean hasUserRetweeted(Long userId, Long tweetId)
    {
        return retweetRepository.existsByOriginalTweetIdAndUserId(tweetId, userId);
    }

    // ----------------------
    // Retweets of tweet
    // ----------------------

    public List<Retweet> getRetweetsOfTweet(Long tweetId)
    {
        return retweetRepository.findByOriginalTweetId(tweetId);
    }

    // ----------------------
    // Retweets of users
    // ----------------------

    public List<Retweet> getRetweetsOfUsers(Set<Long> userIds)
    {
        return retweetRepository.findByUserIds(userIds);
    }

    // ----------------------
    // All retweets
    // ----------------------

    public List<Retweet> getAllRetweets()
    {
        return retweetRepository.findAll();
    }
}
