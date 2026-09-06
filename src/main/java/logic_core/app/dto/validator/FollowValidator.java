package logic_core.app.dto.validator;

import logic_core.common.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class FollowValidator
{
    public void validate(UUID followerId, UUID followingId)
    {
        Objects.requireNonNull(followerId, "FollowerId must not be null.");
        Objects.requireNonNull(followingId, "FollowingId must not be null.");

        if (followerId.equals(followingId))
        {
            throw new ValidationException("Users cannot follow themselves.");
        }
    }
}