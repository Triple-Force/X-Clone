package logic_core.infrastructure.mapper;

import Shared.Models.Media.Media;
import Shared.Models.Tweet.Tweet;
import logic_core.domain.model.MediaModel;

public class MediaPersistenceMapper
{
    public MediaModel toDomain(Media entity)
    {
        if (entity == null)
        {
            return null;
        }

        return MediaModel.builder()
                .mediaId(entity.getId())
                .tweetId(entity.getTweet().getId())
                .mediaUrl(entity.getMediaURL())
                .originalFilename(entity.getOriginalFilename())
                .fileSizeBytes(entity.getFileSizeBytes())
                .mediaType(entity.getMediaType())
                .displayOrder(entity.getDisplayOrder())
                .build();
    }

    public Media toEntity(MediaModel model, Tweet tweet)
    {
        if (model == null)
        {
            return null;
        }

        return Media.builder()
                .tweet(tweet)
                .mediaURL(model.getMediaUrl())
                .originalFilename(model.getOriginalFilename())
                .fileSizeBytes(model.getFileSizeBytes())
                .mediaType(model.getMediaType())
                .displayOrder(model.getDisplayOrder())
                .build();
    }
}
