package logic_core.infrastructure.mapper;

import logic_core.domain.model.MediaModel;
import logic_core.infrastructure.persistence.entity.media.MediaEntity;
import logic_core.domain.model.media.MediaType;
import logic_core.infrastructure.persistence.entity.tweet.TweetEntity;

public final class MediaEntityMapper {

    private MediaEntityMapper() {
    }

    /**
     * Maps a MediaEntity to the domain MediaModel.
     * 
     * Extracts tweet UUID from the entity's TweetEntity reference
     * and preserves all media metadata.
     */
    public static MediaModel toDomain(MediaEntity entity) {
        if (entity == null) {
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

    /**
     * Creates a new MediaEntity from a media URL and tweet reference.
     * 
     * Used by createMedia() to create media records.
     * The caller must provide a resolved TweetEntity reference.
     */
    public static MediaEntity toPersistence(
            String mediaUrl,
            TweetEntity tweet,
            short displayOrder) {
        if (mediaUrl == null || tweet == null) {
            return null;
        }

        MediaEntity entity = new MediaEntity();
        entity.setTweet(tweet);
        entity.setMediaURL(mediaUrl);
        entity.setOriginalFilename(null);
        entity.setFileSizeBytes(0L);
        entity.setMediaType(MediaType.IMAGE);
        entity.setDisplayOrder(displayOrder);
        return entity;
    }
}
