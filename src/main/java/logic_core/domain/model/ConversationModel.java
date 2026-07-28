package logic_core.domain.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ConversationModel
{
    private UUID conversationId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<UUID> participantIds;

    public void addParticipant(UUID userId)
    {
        if (!participantIds.contains(userId))
        {
            participantIds.add(userId);
            this.updatedAt = OffsetDateTime.now();
        }
    }

    public void removeParticipant(UUID userId)
    {
        participantIds.remove(userId);
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean hasParticipant(UUID userId)
    {
        return participantIds.contains(userId);
    }
}