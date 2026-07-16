package logic_core.app.dto.validator;

import logic_core.common.exception.ValidationException;

import java.util.Objects;
import java.util.UUID;

public class MessageValidator
{
    private static final int MAX_MESSAGE_LENGTH = 1000;

    public void validate(UUID senderId, UUID receiverId, String content)
    {
        Objects.requireNonNull(senderId, "senderId must not be null.");
        Objects.requireNonNull(senderId, "receiverId must not be null.");

        if (senderId.equals(receiverId))
        {
            throw new ValidationException("You cannot send direct messages to yourself.");
        }

        if (content == null || content.isBlank())
        {
            throw new ValidationException("Message content cannot be empty.");
        }

        if (content.length() > MAX_MESSAGE_LENGTH)
        {
            throw new ValidationException(
                    String.format("Message content cannot exceed %d characters.", MAX_MESSAGE_LENGTH)
            );
        }
    }
}
