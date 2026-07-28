package logic_core.domain.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.*;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TweetModel
{
    private UUID id;
    private UUID authorId;
    private String content;

    private UUID repliedToTweetId;
    private UUID retweetedTweetId;
    private UUID quotedTweetId;

    private boolean deleted;
    private boolean pinned;
    private OffsetDateTime scheduledAt;
    private OffsetDateTime publishedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private long likeCount;
    private long replyCount;
    private long retweetCount;
    private boolean isEdited;

    private List<UUID> mentionedUserIds;
    private List<UUID> mediaIds;

    public void addMentionUserId(UUID userId)
    {
        validateMentionUserId(userId);

        if (mentionedUserIds == null)
        {
            mentionedUserIds = new ArrayList<>();
        }
        else if (!(mentionedUserIds instanceof ArrayList))
        {
            mentionedUserIds = new ArrayList<>(mentionedUserIds);
        }

        boolean exists = mentionedUserIds.stream()
                .filter(Objects::nonNull)
                .anyMatch(id -> Objects.equals(id, userId));

        if (!exists)
        {
            mentionedUserIds.add(userId);
        }
    }

    public void removeMentionUserId(UUID userId)
    {
        validateMentionUserId(userId);

        if (mentionedUserIds == null || mentionedUserIds.isEmpty())
        {
            return;
        }

        if (!(mentionedUserIds instanceof ArrayList))
        {
            mentionedUserIds = new ArrayList<>(mentionedUserIds);
        }

        mentionedUserIds.removeIf(id -> Objects.equals(id, userId));
    }

    public void setMentionedUserIds(List<UUID> userIds)
    {
        if (userIds == null || userIds.isEmpty())
        {
            this.mentionedUserIds = new ArrayList<>();
            return;
        }

        this.mentionedUserIds = userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    public List<UUID> getMentionedUserIdsSafe()
    {
        if (mentionedUserIds == null || mentionedUserIds.isEmpty())
        {
            return List.of();
        }
        return Collections.unmodifiableList(mentionedUserIds);
    }

    private static void validateMentionUserId(UUID userId)
    {
        if (userId == null)
        {
            throw new IllegalArgumentException("Mentioned user ID cannot be null");
        }
    }
}
