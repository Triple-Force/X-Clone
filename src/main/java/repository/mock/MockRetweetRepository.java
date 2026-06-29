package repository.mock;

import model.Retweet;
import repository.RetweetRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MockRetweetRepository implements RetweetRepository
{

    private final List<Retweet> retweets = new ArrayList<>();
    private Long idSequence = 1L;

    @Override
    public Retweet save(Retweet retweet)
    {
        if (retweet.getId() == null)
        {
            retweet.setId(idSequence++);
        }

        if (retweet.getCreatedAt() == null)
        {
            retweet.setCreatedAt(LocalDateTime.now());
        }

        retweets.add(retweet);
        return retweet;
    }

    @Override
    public List<Retweet> findAll()
    {
        return retweets.stream()
                .sorted(Comparator.comparing(Retweet::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByOriginalTweetIdAndUserId(Long originalTweetId, Long userId)
    {
        retweets.removeIf(r ->
                r.getOriginalTweetId().equals(originalTweetId)
                        && r.getUserId().equals(userId)
        );
    }

    @Override
    public boolean existsByOriginalTweetIdAndUserId(Long originalTweetId, Long userId)
    {
        return retweets.stream()
                .anyMatch(r ->
                        r.getOriginalTweetId().equals(originalTweetId)
                                && r.getUserId().equals(userId)
                );
    }

    @Override
    public List<Retweet> findByUserIds(Set<Long> userIds)
    {
        return retweets.stream()
                .filter(r -> userIds.contains(r.getUserId()))
                .sorted(Comparator.comparing(Retweet::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Retweet> findByOriginalTweetId(Long originalTweetId)
    {
        return retweets.stream()
                .filter(r -> r.getOriginalTweetId().equals(originalTweetId))
                .sorted(Comparator.comparing(Retweet::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Retweet> findByOriginalTweetIdAndUserId(Long originalTweetId, Long userId)
    {
        return retweets.stream()
                .filter(r ->
                        r.getOriginalTweetId().equals(originalTweetId)
                                && r.getUserId().equals(userId)
                )
                .findFirst();
    }
}
