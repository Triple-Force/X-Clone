package service;

import lombok.RequiredArgsConstructor;
import model.Tweet;
import repository.LikeRepository;
import repository.TweetRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class TweetService
{
    private final TweetRepository tweetRepository;
    private final LikeRepository likeRepository;



    // ----------------------
    // Create normal tweet
    // ----------------------
    public Tweet createTweet(Long authorId, String content)
    {
        Tweet tweet = Tweet.builder()
                .authorId(authorId)
                .content(content)
                .build();

        return tweetRepository.save(tweet);
    }

    // ----------------------
    // Quote tweet
    // ----------------------
    public Tweet quoteTweet(Long authorId, Long quotedTweetId, String content)
    {
        Tweet original = tweetRepository.findById(quotedTweetId)
                .orElseThrow(() -> new RuntimeException("Tweet not found"));

        Tweet quoteTweet = Tweet.builder()
                .authorId(authorId)
                .content(content)
                .quotedTweetId(original.getId())
                .build();

        return tweetRepository.save(quoteTweet);
    }

    // ----------------------
    // Delete tweet
    // ----------------------
    public void deleteTweet(Long tweetId)
    {
        tweetRepository.deleteById(tweetId);
    }

    // ----------------------
    // Like/unlike
    // ----------------------
    public boolean toggleLike(Long tweetId, Long userId)
    {
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new RuntimeException("Tweet not found"));

        if (likeRepository.isLikedByUser(userId, tweetId))
        {
            likeRepository.unlike(userId, tweetId);
            return false;
        }

       likeRepository.like(userId, tweetId);
        return true;
    }

    // ----------------------
    // Get single tweet
    // ----------------------
    public Optional<Tweet> getTweet(Long tweetId)
    {
        return tweetRepository.findById(tweetId);
    }

    // ----------------------
    // Tweets of a user
    // ----------------------
    public List<Tweet> getUserTweets(Long userId)
    {
        return tweetRepository.findAuthorTweets(userId);
    }

    // ----------------------
    // Timeline
    // ----------------------
    public List<Tweet> getTimeline(Set<Long> followingIds)
    {
        return tweetRepository.findFollowingTweets(followingIds);
    }

    // ----------------------
    // Debug
    // ----------------------
    public List<Tweet> getAllTweets()
    {
        return tweetRepository.findAll();
    }
}
