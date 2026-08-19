package logic_core.app.dto.response;

import Shared.Models.Media.MediaType;

import java.util.UUID;

public record DownloadMediaResponse(
        UUID id,
        String mediaUrl,
        String originalFilename,
        Long fileSizeBytes,
        MediaType mediaType
) {}