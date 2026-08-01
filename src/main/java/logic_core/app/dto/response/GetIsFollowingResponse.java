package logic_core.app.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record GetIsFollowingResponse(

        boolean following

) {}