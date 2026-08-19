package logic_core.domain.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MuteRelation
{
    private UUID muterId;
    private UUID mutedId;
    private OffsetDateTime createdAt;

    public static MuteRelation create(UUID muterId, UUID mutedId, OffsetDateTime time)
    {
        Objects.requireNonNull(mutedId, "MuteRelation.mutedId cannot be null");
        Objects.requireNonNull(muterId, "MuteRelation.muterId cannot be null");
        Objects.requireNonNull(time, "mute.createdAt cannot be null.");

        return MuteRelation.builder()
                .muterId(muterId)
                .mutedId(mutedId)
                .createdAt(time)
                .build();
    }
}