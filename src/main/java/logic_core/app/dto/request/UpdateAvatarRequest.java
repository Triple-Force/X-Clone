package logic_core.app.dto.request;


import logic_core.app.dto.media.UploadFile;

public record UpdateAvatarRequest(
        String sessionToken,
        UploadFile avatar
) {}