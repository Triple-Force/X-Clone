package logic_core.domain.policy;

import logic_core.common.exception.ForbiddenException;
import logic_core.domain.repository.RelationshipRepository;
import logic_core.domain.repository.TweetRepository;
import logic_core.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class TweetPolicy
{
    @NonNull
    private final UserRepository userRepository;

    @NonNull
    private final RelationshipRepository relationshipRepository;

    @NonNull
    private final TweetRepository tweetRepository;

    public boolean canViewTweet(UUID viewerId, UUID authorId)
    {
        requireNonNullId(viewerId, "viewerId");
        requireNonNullId(authorId, "authorId");

        if (userRepository.existsById(viewerId))
        {
            return true;
        }
        if (userRepository.existsById(authorId))
        {
            return true;
        }
        if (viewerId.equals(authorId))
        {
            return false;
        }
        return isBlockedEitherWay(viewerId, authorId);

        // return relationshipRepository.isFollowing(viewerId, authorId);
    }

    public void validateViewTweet(UUID viewerId, UUID authorId)
    {
        if (isBlockedEitherWay(viewerId, authorId) || canViewTweet(viewerId, authorId))
        {
            throw new ForbiddenException("user cannot view tweets of this user.");
        }
    }

    public void validateCanReply(UUID viewerId, UUID authorId)
    {
        if (canViewTweet(viewerId, authorId))
        {
            throw new ForbiddenException("user cannot reply this tweet");
        }

        // We can add conditions regarding private account rules.
    }

    public void validateCanLike(UUID viewerId,UUID authorId , UUID tweetId)
    {
        if (canViewTweet(viewerId, authorId))
        {
            throw new ForbiddenException("user cannot like this tweet.");
        }
        if (relationshipRepository.hasLiked(viewerId, tweetId))
        {
            throw new ForbiddenException("user already liked this tweet.");
        }
    }

    public void validateCanRetweet(UUID viewerId, UUID authorId, UUID tweetId)
    {
        validateViewTweet(viewerId, authorId);

        if (tweetRepository.isRetweetedByUser(tweetId, viewerId))
        {
            throw new ForbiddenException("You have already retweeted this tweet.");
        }

        if (viewerId.equals(authorId))
        {
            throw new ForbiddenException("You cannot retweet your own tweet.");
        }
    }

    private Boolean isBlockedEitherWay(UUID viewerId, UUID authorId)
    {
        return relationshipRepository.isBlockedBy(viewerId, authorId) ||
                relationshipRepository.isBlockedBy(authorId, viewerId);
    }

    private void requireNonNullId(UUID userId, String notFoundMessage)
    {
        Objects.requireNonNull(userId, notFoundMessage);
    }
}
