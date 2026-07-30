package logic_core.app.dto.response;

import java.util.UUID;

public record UpdateBannerResponse(
        UUID userId,
        String bannerUrl
) {}