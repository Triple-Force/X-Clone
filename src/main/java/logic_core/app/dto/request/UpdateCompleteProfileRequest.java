package logic_core.app.dto.request;

import logic_core.app.dto.media.UploadFile;

import java.util.UUID;

public record UpdateCompleteProfileRequest(

        String sessionToken,

        UUID userId,

        String displayName,

        String username,

        String bio,

        UploadFile avatar,

        UploadFile banner

) {}