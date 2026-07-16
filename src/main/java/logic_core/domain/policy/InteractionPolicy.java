package logic_core.domain.policy;

import logic_core.common.exception.ConflictException;
import logic_core.common.exception.ForbiddenException;
import logic_core.common.exception.NotFoundException;
import logic_core.domain.model.UserModel;
import logic_core.domain.repository.RelationshipRepository;
import logic_core.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class InteractionPolicy
{
    @NonNull
    private final UserRepository userRepository;

    @NonNull
    private final RelationshipRepository relationshipRepository;

    public void validateLike(UUID userId, UUID tweetId, UUID tweetAuthorId)
    {
        requireNonNullId(userId, "userId");
        requireNonNullId(tweetId, "tweetId");

        validateBaseInteraction(userId, tweetAuthorId);

        if (relationshipRepository.hasLiked(userId, tweetId))
        {
            throw new ConflictException("Like relation already exists.");
        }
    }

    public void validateUnlike(UUID userId, UUID tweetId, UUID tweetAuthorId)
    {

        requireNonNullId(userId, "userId");
        requireNonNullId(tweetId, "tweetId");
        requireNonNullId(tweetAuthorId ,"tweetAuthorId");

        validateBlockBarrier(userId, tweetAuthorId);

        if (!relationshipRepository.hasLiked(userId, tweetId))
        {
            throw new NotFoundException("Like relation does not exist.");
        }
    }

    public void validateReply(UUID userId, UUID tweetAuthorId)
    {
        requireNonNullId(userId, "userId");
        requireNonNullId(tweetAuthorId ,"tweetAuthorId");

        validateBaseInteraction(userId, tweetAuthorId);
    }

    public void validateRetweet(UUID userId, UUID tweetAuthorId)
    {
        requireNonNullId(userId, "userId");
        requireNonNullId(tweetAuthorId ,"tweetAuthorId");

        validateBaseInteraction(userId, tweetAuthorId);
    }

    private void validateBaseInteraction(UUID userId, UUID tweetAuthorId)
    {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));
        UserModel author = userRepository.findById(tweetAuthorId)
                .orElseThrow(() -> new NotFoundException("Tweet author not found."));

        if (!user.isActive())
        {
            throw new ForbiddenException("Inactive users cannot interact.");
        }

        if (!author.isActive())
        {
            throw new ForbiddenException("Cannot interact with content of an inactive user.");
        }

        validateBlockBarrier(userId, tweetAuthorId);
    }

    private void validateBlockBarrier(UUID userId, UUID tweetAuthorId)
    {
        if (relationshipRepository.existsBlockRelation(userId, tweetAuthorId))
        {
            throw new ForbiddenException("Interaction is not allowed because a block relation exists between users.");
        }
    }

    private void requireNonNullId(UUID id, String name)
    {
        Objects.requireNonNull(id, name + " must not be null");
    }
}