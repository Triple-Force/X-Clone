package logic_core.app.dto.request;

import logic_core.app.dto.media.UploadFile;

public record UpdateBannerRequest(
        String sessionToken,
        UploadFile banner
) {}