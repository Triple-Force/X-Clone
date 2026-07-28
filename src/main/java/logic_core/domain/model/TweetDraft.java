package logic_core.domain.model;

import lombok.Builder;
import lombok.Value;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Value
@Builder
public class TweetDraft
{
    UUID authorId;
    String content;
    UUID repliedToTweetId;
    UUID retweetedTweetId;
    UUID quotedTweetId;
    OffsetDateTime scheduledAt;
    List<MediaModel> media;
    PollModel poll;
}
