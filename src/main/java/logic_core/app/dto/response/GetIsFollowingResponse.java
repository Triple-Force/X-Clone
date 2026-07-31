package logic_core.app.dto.response;

import java.util.UUID;

public record GetIsFollowingResponse (
        UUID userId,
        UUID targetId
)
{
}
