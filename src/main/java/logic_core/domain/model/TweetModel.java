package logic_core.domain.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
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


    public TweetModel withMentionedUserIds(List<UUID> mentionedUserIds)
    {
        return TweetModel.builder()
                .id(this.id)
                .authorId(this.authorId)
                .content(this.content)
                .repliedToTweetId(this.repliedToTweetId)
                .retweetedTweetId(this.retweetedTweetId)
                .quotedTweetId(this.quotedTweetId)
                .pinned(this.pinned)
                .deleted(this.deleted)
                .scheduledAt(this.scheduledAt)
                .publishedAt(this.publishedAt)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .isEdited(this.isEdited)
                .mentionedUserIds(mentionedUserIds != null ? List.copyOf(mentionedUserIds) : List.of())
                .build();
    }

    public void addMentionUserId(UUID mentionUserId)
    {

        if (mentionUserId == null)
            throw new IllegalArgumentException("mentionedUserId cannot be null");

        for (UUID id : mentionedUserIds)
            if (id == mentionUserId)
                throw new IllegalArgumentException("this user already mentioned");

        mentionedUserIds.add(mentionUserId);
    }
}
