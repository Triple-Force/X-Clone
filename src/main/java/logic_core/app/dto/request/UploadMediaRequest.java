package logic_core.app.dto.request;

import logic_core.app.dto.media.UploadFile;

import java.util.List;
import java.util.UUID;

public record UploadMediaRequest(
        String sessionToken,
        UploadFile file,
        UUID tweetId,
        List<String> uploadTokens
) {}