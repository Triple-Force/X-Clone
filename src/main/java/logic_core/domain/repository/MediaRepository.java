package logic_core.domain.repository;

import logic_core.domain.model.MediaModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MediaRepository
{
    Optional<MediaModel> findById(UUID mediaId);

    List<MediaModel> findByIds(List<UUID> mediaIds);

    boolean existsById(UUID mediaId);

    boolean belongsToUser(UUID mediaId, UUID userId);

    boolean isAlreadyAttached(UUID mediaId);

}
