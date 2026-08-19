package logic_core.app.dto.request;

import lombok.Builder;

import java.util.UUID;

@Builder
public record GetIsLikedRequest(

        String sessionToken,

        UUID tweetId

) {
}