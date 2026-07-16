package logic_core.domain.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class HashtagModel
{
    private UUID hashtagId;
    private String tag;
    private OffsetDateTime createdAt;
    private long usageCount;

    public static String normalizeHashtag(String tag)
    {
        return tag.toLowerCase().replaceAll("[^a-z0-9_]", "");
    }
}
