package repository.mock;

import model.Tweet;
import repository.TweetRepository;

import java.util.*;
import java.util.stream.Collectors;

public class MockTweetRepository implements TweetRepository
{
    //simulation
    private List<Tweet> tweets = new ArrayList<>();
    private long idCounter = 1;

    @Override
    public Tweet save(Tweet tweet)
    {
        if (tweet.getId() == null)
        {
            tweet.setId(idCounter++);
            tweet.setCreatedAt(java.time.LocalDateTime.now());
        }

        tweets.removeIf(t -> t.getId().equals(tweet.getId()));
        tweets.add(tweet);

        return tweet;
    }

    @Override
    public List<Tweet> findAll()
    {
        return tweets.stream()
                .sorted(Comparator.comparing(Tweet::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Tweet> findAuthorTweets(Long userId)
    {
        return tweets.stream()
                .filter(t -> t.getAuthorId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Tweet> findFollowingTweets(Set<Long> authorIds)
    {
        return tweets.stream()
                .filter(t -> authorIds.contains(t.getAuthorId()))
                .sorted(Comparator.comparing(Tweet::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }


    @Override
    public void deleteById(Long id)
    {
        tweets.removeIf(t -> t.getId().equals(id));
    }


    @Override
    public Optional<Tweet> findById(Long id)
    {
      return tweets.stream()
              .filter(t -> t.getId().equals(id))
              .findFirst();
    }

    @Override
    public List<Tweet> findByIds(Set<Long> ids)
    {
        if (ids == null || ids.isEmpty())
        {
            return Collections.emptyList();
        }

        return tweets.stream()
                .filter(tweet -> ids.contains(tweet.getId()))
                .collect(Collectors.toList());
    }
}
