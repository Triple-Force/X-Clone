package logic_core.app.dto.validator;

import logic_core.common.exception.ValidationException;
import logic_core.domain.repository.TimelineType;

import java.util.UUID;

public class TimelineValidator
{
    public void validate(TimelineType timelineType, UUID actorId, int page, int pageSize, UUID targetUserId)
    {

        requireNonNull(timelineType, "Timeline type is required.");
        requireNonNull(actorId, "Actor id is required.");

        if (page < 0)
        {
            throw new ValidationException("Page index must be non-negative.");
        }

        if (pageSize <= 0)
        {
            throw new ValidationException("Page size must be greater than zero.");
        }

        if (pageSize > 100)
        {
            throw new ValidationException("Maximum page size is 100.");
        }

        switch (timelineType)
        {
            case USER:
            case REPLIES:
            case MEDIA:
            case LIKED:

                if (targetUserId == null)
                {
                    throw new ValidationException(
                            "Target user id is required."
                    );
                }

                break;

            case HOME:
            case FOLLOWING:
                break;
        }
    }

    private void requireNonNull(Object value, String message)
    {
        if (value == null)
        {
            throw new ValidationException(message);
        }
    }
}