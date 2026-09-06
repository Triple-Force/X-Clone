package logic_core.app.dto.validator;

import logic_core.common.exception.ValidationException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class TweetValidator
{
    private static final int MAX_TWEET_LENGTH = 280;
    private static final int MAX_MEDIA_COUNTS = 4;

    public void validateCreateTweet(String content,
            UUID replyToId,
            UUID quoteOfId,
            List<String> mediaUrls,
            boolean hasPoll,
            OffsetDateTime scheduledAt
    )
    {
        boolean hasAttachments = (mediaUrls != null && !mediaUrls.isEmpty()) || hasPoll || quoteOfId != null;

        if (!hasAttachments && (content == null || content.trim().isEmpty()))
        {
            throw new ValidationException(
                    "tweet.content.required"
            );
        }

        validateContentLength(content);

        validateUploadTokens(mediaUrls);

        validateScheduledAt(scheduledAt);

        if (replyToId != null && quoteOfId != null)
        {
            throw new ValidationException("tweet.reply_and_quote.conflict");
        }
    }


    public void validateReplyTweet(UUID parentTweetId, String text, List<String> uploadTokens)
    {
        requireNonNull(parentTweetId, "reply.parentTweetId.required");


        boolean hasMedia = uploadTokens != null && !uploadTokens.isEmpty();


        if (!hasMedia && (text == null || text.trim().isEmpty()))
        {
            throw new ValidationException("reply.text.or.media.required");
        }
        validateContentLength(text);

        validateUploadTokens(uploadTokens);
    }

    public void validateSearchTweet(String query)
    {
        if (query == null || query.trim().isEmpty())
        {
            throw new ValidationException("tweet.search.query.required");
        }
        if (query.trim().length() > MAX_TWEET_LENGTH)
        {
            throw new ValidationException("tweet.search.query.too.long");
        }
    }

    public void validateGetTweetDetailsTweet(UUID tweetId)
    {
        requireNonNull(tweetId, "tweet.id.required");
    }

    public void validateGetTimelineTweet(UUID userId)
    {
        requireNonNull(userId, "timeline.userId.required");
    }

    public void validateEditTweet(UUID tweetId, String content)
    {
        requireNonNull(tweetId, "tweet.id.required");

        if (content == null || content.trim().isEmpty())
        {
            throw new ValidationException("tweet.content.required");
        }

        validateContentLength(content);
    }

    private void validateContentLength(String content)
    {
        if (content != null && content.length() > MAX_TWEET_LENGTH)
        {
            throw new ValidationException("Tweet content cannot exceed " + MAX_TWEET_LENGTH + " characters."
            );
        }
    }

    private void validateUploadTokens(List<String> uploadTokens)
    {
        if (uploadTokens == null || uploadTokens.isEmpty())
        {
            return;
        }


        if (uploadTokens.contains(null))
        {
            throw new ValidationException("tweet.uploadTokens.cannot.contain.null");
        }


        if (uploadTokens.stream().anyMatch(String::isBlank))
        {
            throw new ValidationException("tweet.uploadTokens.cannot.contain.blank");
        }


        if (uploadTokens.size() > MAX_MEDIA_COUNTS)
        {
            throw new ValidationException("tweet.media.max." + MAX_MEDIA_COUNTS);
        }
    }

    private void validateScheduledAt(OffsetDateTime scheduledAt)
    {
        if (scheduledAt == null)
        {
            return;
        }
        OffsetDateTime now = OffsetDateTime.now();


        if (scheduledAt.isBefore(now.plusMinutes(1)))
        {
            throw new ValidationException("Scheduled time must be in the future.");
        }


        if (scheduledAt.isAfter(now.plusDays(30)))
        {
            throw new ValidationException("Scheduled time is too far in the future.");
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