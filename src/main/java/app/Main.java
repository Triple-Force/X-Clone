package app;

import model.*;
import repository.*;
import repository.mock.MockLikeRepository;
import repository.mock.MockRetweetRepository;
import repository.mock.MockTweetRepository;
import service.*;
import util.TimelinePrinter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main
{
    public static void main(String[] args)
    {
        TweetRepository tweetRepository = new MockTweetRepository();
        RetweetRepository retweetRepository = new MockRetweetRepository();
        LikeRepository likeRepository = new MockLikeRepository();


        TimelineService timelineService = new TimelineService(tweetRepository, retweetRepository, likeRepository);

        TimelinePrinter printer = new TimelinePrinter();

        // users
        Long userA = 1L;
        Long userB = 2L;
        Long userC = 3L;

        // -------------------
        // create tweets
        // -------------------

        Tweet tweet1 = Tweet.builder()
                .id(1L)
                .authorId(userB)
                .content("Hello Twitter clone!")
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .build();

        Tweet tweet2 = Tweet.builder()
                .id(2L)
                .authorId(userC)
                .content("Java backend is fun")
                .createdAt(LocalDateTime.now().minusMinutes(8))
                .build();

        tweetRepository.save(tweet1);
        tweetRepository.save(tweet2);

        // -------------------
        // quote tweet
        // -------------------

        Tweet quoteTweet = Tweet.builder()
                .id(3L)
                .authorId(userB)
                .content("This is interesting")
                .quotedTweetId(2L)
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .build();

        tweetRepository.save(quoteTweet);

        // -------------------
        // retweet
        // -------------------

        Retweet retweet = Retweet.builder()
                .id(1L)
                .userId(userC)
                .originalTweetId(1L)
                .createdAt(LocalDateTime.now().minusMinutes(3))
                .build();

        retweetRepository.save(retweet);

        // -------------------
        // likes
        // -------------------

        likeRepository.like(userA, 1L);

        likeRepository.like(userA, 2L);

        likeRepository.like(99L, 3L);

        // -------------------
        // following
        // -------------------

        Set<Long> followingIds = new HashSet<>();
        followingIds.add(userB);
        followingIds.add(userC);

        // -------------------
        // get timeline
        // -------------------

        List<TimelineItem> timeline =
                timelineService.getTimeline(userA, followingIds);

        Retweet myRetweet = Retweet.builder()
                .id(10L)
                .userId(1L)
                .originalTweetId(2L)
                .createdAt(LocalDateTime.now())
                .build();
        retweetRepository.save(myRetweet);

        // -------------------
        // print timeline
        // -------------------

        printer.print(timeline);

    }
}
