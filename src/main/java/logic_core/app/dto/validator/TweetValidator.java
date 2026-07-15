package logic_core.app.dto.validator;

import logic_core.common.exception.ValidationException;

import java.util.UUID;

public class TweetValidator
{
    private static final int MAX_TWEET_LENGTH = 280;

    public void validate(UUID quoteOfId, UUID replyToId, String content)
    {
        if (quoteOfId != null || replyToId == null)
        {
            if (content == null || content.trim().isEmpty())
            {
                throw new ValidationException("tweet.content.required");
            }
        }

        if (content.length() > MAX_TWEET_LENGTH)
        {
            throw new ValidationException(
                    String.format("Tweet content cannot exceed %d characters.", MAX_TWEET_LENGTH)
            );
        }
    }
}