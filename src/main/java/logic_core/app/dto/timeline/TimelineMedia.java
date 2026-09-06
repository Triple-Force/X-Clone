package logic_core.app.dto.timeline;


import logic_core.domain.model.media.MediaType;
import lombok.Builder;

import java.util.UUID;

@Builder
public record TimelineMedia(
        UUID mediaId,
        String mediaUrl,
        MediaType mediaType,
        short displayOrder
) {}