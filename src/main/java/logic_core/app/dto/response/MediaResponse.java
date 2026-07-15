package logic_core.app.dto.response;

import Shared.Models.Media.MediaType;
import lombok.Builder;

import java.util.UUID;

@Builder
public record MediaResponse(
        UUID mediaId,
        String mediaUrl,
        String originalFilename,
        Long fileSizeBytes,
        MediaType mediaType, short displayOrder
) {}
