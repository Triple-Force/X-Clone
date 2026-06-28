package model;

import enums.TimelineItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimelineItem
{
    private TimelineItemType type;

    private Long actorUserId;

    private Tweet tweet;

    private Tweet originalTweet;

    private boolean likedByCurrentUser;

    private boolean retweetedByCurrentUser;

    private int likeCount;

    private LocalDateTime createdAt;

}
