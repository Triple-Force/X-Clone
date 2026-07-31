package logic_core.infrastructure.repository;

import logic_core.domain.model.MediaModel;
import logic_core.domain.repository.MediaRepository;
import logic_core.infrastructure.dao.MediaDao;
import logic_core.infrastructure.mapper.MediaPersistenceMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaMediaRepository implements MediaRepository
{

    private final MediaDao mediaDao;

    public JpaMediaRepository(MediaDao mediaDao)
    {
        this.mediaDao = mediaDao;
    }

    @Override
    public Optional<MediaModel> findById(UUID mediaId)
    {
        return mediaDao.findById(mediaId).map(MediaPersistenceMapper::toDomain);
    }

    @Override
    public List<MediaModel> findByIds(List<UUID> mediaIds)
    {
        return mediaDao.findByIds(mediaIds).stream().map(MediaPersistenceMapper::toDomain).toList();
    }

    @Override
    public boolean existsById(UUID mediaId)
    {
        return mediaDao.existsById(mediaId);
    }

    @Override
    public boolean belongsToUser(UUID mediaId, UUID userId)
    {
        return mediaDao.belongsToUser(mediaId, userId);
    }

    @Override
    public boolean isAlreadyAttached(UUID mediaId)
    {
        return mediaDao.isAlreadyAttached((mediaId));
    }

    @Override
    public List<MediaModel> createMedia(UUID tweetId, List<String> uploadTokens) {
        return List.of();
    }

    public void delete(UUID mediaId) {

    }
}
