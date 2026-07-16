package logic_core.app.dto.request;

import Shared.Models.Media.MediaType;
import lombok.Builder;

@Builder
public record CreateMediaRequest(
        String mediaUrl,
        String originalFilename,
        Long fileSizeBytes,
        MediaType mediaType,
        Short displayOrder
) {}
