package logic_core.infrastructure.dao;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Media.Media;
import Shared.Models.Media.MediaType;
import Shared.Models.Tweet.Tweet;
import logic_core.domain.model.MediaModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MediaDao extends GenericDAO<Media>
{
    public MediaDao()
    {
        super(Media.class);
    }

    public List<Media> findByTweetId(UUID tweetId)
    {
        return findByJpql(
                "SELECT m FROM Media m WHERE m.tweet.id = :tweetId",
                query -> query.setParameter("tweetId", tweetId)
        );
    }

    public Optional<Media> findById(UUID mediaId)
    {
        return Optional.ofNullable(
                findOneByJpql(
                        """
                        SELECT m
                        FROM Media m
                        WHERE m.id = :mediaId
                        """,
                        q -> q.setParameter("mediaId", mediaId)
                )
        );
    }

    public List<Media> findByIds(List<UUID> mediaIds)
    {
        if (mediaIds == null || mediaIds.isEmpty())
        {
            return List.of();
        }

        return findByJpql(
                """
                SELECT m
                FROM Media m
                WHERE m.id IN :mediaIds
                ORDER BY m.displayOrder
                """,
                q -> q.setParameter("mediaIds", mediaIds)
        );
    }

    public boolean existsById(UUID mediaId)
    {
        return countByJpql(
                """
                SELECT COUNT(m)
                FROM Media m
                WHERE m.id = :mediaId
                """,
                q -> q.setParameter("mediaId", mediaId)
        ) > 0;
    }

    public boolean belongsToUser(UUID mediaId, UUID userId)
    {
        return countByJpql(
                """
                SELECT COUNT(m)
                FROM Media m
                WHERE m.id = :mediaId
                  AND m.tweet IS NULL
                  AND EXISTS (
                        SELECT 1
                        FROM Upload u
                        WHERE u.media.id = m.id
                          AND u.user.id = :userId
                  )
                """,
                q -> q.setParameter("mediaId", mediaId)
                        .setParameter("userId", userId)
        ) > 0;
    }

    public boolean isAlreadyAttached(UUID mediaId)
    {
        return countByJpql(
                """
                SELECT COUNT(m)
                FROM Media m
                WHERE m.id = :mediaId
                  AND m.tweet IS NOT NULL
                """,
                q -> q.setParameter("mediaId", mediaId)
        ) > 0;
    }

    public List<MediaModel> createMedia(
            UUID tweetId,
            List<String> mediaUrls)
    {
        if (mediaUrls == null || mediaUrls.isEmpty())
        {
            return List.of();
        }

        Tweet tweet =
                getEntityManager().getReference(
                        Tweet.class,
                        tweetId
                );

        short order = 0;

        List<MediaModel> result = new java.util.ArrayList<>();

        for (String url : mediaUrls)
        {
            Media media =
                    Media.builder()
                            .tweet(tweet)
                            .mediaURL(url)
                            .originalFilename(null)
                            .fileSizeBytes(0L)
                            .mediaType(MediaType.IMAGE) // فعلاً
                            .displayOrder(order++)
                            .build();

            super.insert(media);

            result.add(
                    MediaModel.builder()
                            .mediaId(media.getId())
                            .tweetId(tweetId)
                            .mediaUrl(media.getMediaURL())
                            .originalFilename(media.getOriginalFilename())
                            .fileSizeBytes(media.getFileSizeBytes())
                            .mediaType(media.getMediaType())
                            .displayOrder(media.getDisplayOrder())
                            .build()
            );
        }

        return result;
    }

    public void delete(UUID mediaId)
    {

    }
}
