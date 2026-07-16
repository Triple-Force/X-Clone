package logic_core.app.mapper;

import logic_core.domain.model.MediaModel;
import logic_core.app.dto.response.MediaResponse;

public class MediaResponseMapper
{
    public MediaResponse toResponse(MediaModel model)
    {
        if (model == null)
        {
            return null;
        }

        return MediaResponse.builder()
                .mediaId(model.getMediaId())
                .mediaUrl(model.getMediaUrl())
                .originalFilename(model.getOriginalFilename())
                .fileSizeBytes(model.getFileSizeBytes())
                .mediaType(model.getMediaType())
                .displayOrder(model.getDisplayOrder())
                .build();
    }
}
