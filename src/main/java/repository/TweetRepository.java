package repository;

import model.Tweet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TweetRepository
{
    Tweet save(Tweet tweet);

    List<Tweet> findAll();

    void deleteById(Long id);

    Optional<Tweet> findById(Long id);

    List<Tweet> findByIds(Set<Long> ids);

    List<Tweet> findAuthorTweets(Long userId);

    List<Tweet> findFollowingTweets(Set<Long> authorIds);
}

