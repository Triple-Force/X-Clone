package logic_core.app.dto.request;

import lombok.Builder;

import java.util.UUID;

@Builder
public record GetIsFollowingRequest(

        String sessionToken,
        UUID targetUserId

) {}