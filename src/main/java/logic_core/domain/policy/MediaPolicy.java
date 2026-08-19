package logic_core.domain.policy;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import logic_core.common.exception.ForbiddenException;
import logic_core.common.exception.NotFoundException;
import logic_core.domain.repository.MediaRepository;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class MediaPolicy
{
    @NonNull
    private final MediaRepository mediaRepository;

    public void validateMediaAttachment(List<UUID> mediaIds, UUID currentUserId)
    {
        if (mediaIds == null || mediaIds.isEmpty())
        {
            return;
        }

        for (UUID mediaId : mediaIds)
        {
            if (mediaId == null)
            {
                continue;
            }

            if (!mediaRepository.existsById(mediaId))
            {
                throw new NotFoundException("Media not found with ID: " + mediaId);
            }

            if (!mediaRepository.belongsToUser(mediaId, currentUserId))
            {
                throw new ForbiddenException("User is not the owner of media ID: " + mediaId);
            }

            if (mediaRepository.isAlreadyAttached(mediaId))
            {
                throw new ForbiddenException("Media with ID " + mediaId + " is already attached to another tweet.");
            }
        }
    }
}
