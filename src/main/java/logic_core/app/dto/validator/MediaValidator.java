package logic_core.app.dto.validator;

import Shared.Models.Media.MediaType;
import logic_core.domain.model.MediaModel;
import logic_core.common.exception.ValidationException;

import java.util.List;

public class MediaValidator
{
    private static final int MAX_MEDIA_COUNT = 4;
    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024; // 5MB
    private static final long MAX_VIDEO_SIZE_BYTES = 50 * 1024 * 1024; // 50MB
    private static final long MAX_GIF_SIZE_BYTES = 15 * 1024 * 1024; // 15MB

    public static void validateMediaSet(List<MediaModel> mediaList)
    {
        if (mediaList == null || mediaList.isEmpty())
        {
            return;
        }

        if (mediaList.size() > MAX_MEDIA_COUNT)
        {
            throw new ValidationException(
                    String.format("The number of media items cannot exceed %d.", MAX_MEDIA_COUNT)
            );
        }

        int imageCount = 0;
        int videoCount = 0;
        int gifCount = 0;

        for (MediaModel media : mediaList)
        {
            validateIndividualMedia(media);

            if (media.getMediaType() == MediaType.IMAGE) imageCount++;
            else if (media.getMediaType() == MediaType.VIDEO) videoCount++;
            else if (media.getMediaType() == MediaType.GIF) gifCount++;
        }

        // قانون ۱۰: ویدیو یا GIF نمی‌توانند با رسانه‌های دیگر ترکیب شوند یا بیش از یکی باشند.
        if (videoCount > 0 && (imageCount > 0 || gifCount > 0 || videoCount > 1))
        {
            throw new ValidationException("A video cannot be combined with images, GIFs, or other videos.");
        }

        if (gifCount > 0 && (imageCount > 0 || videoCount > 0 || gifCount > 1))
        {
            throw new ValidationException("A GIF file cannot be combined with other images, videos, or GIFs.");
        }

        validateDisplayOrders(mediaList);
    }

    private static void validateIndividualMedia(MediaModel media)
    {
        if (media == null)
        {
            throw new ValidationException("Media item cannot be null.");
        }

        if (media.getMediaUrl() == null || media.getMediaUrl().strip().isEmpty())
        {
            throw new ValidationException("The media URL is required.");
        }

        if (media.getFileSizeBytes() == null || media.getFileSizeBytes() <= 0)
        {
            throw new ValidationException("The media file size is invalid.");
        }

        MediaType type = media.getMediaType();
        if (type == null)
        {
            throw new ValidationException("The media type (MediaType) has not been specified.");
        }

        long size = media.getFileSizeBytes();
        switch (type)
        {
            case IMAGE:
                if (size > MAX_IMAGE_SIZE_BYTES)
                {
                    throw new ValidationException("The image size cannot exceed 5 MB.");
                }
                break;
            case VIDEO:
                if (size > MAX_VIDEO_SIZE_BYTES)
                {
                    throw new ValidationException("The video size cannot exceed 50 MB.");
                }
                break;
            case GIF:
                if (size > MAX_GIF_SIZE_BYTES)
                {
                    throw new ValidationException("The GIF file size cannot exceed 15 MB.");
                }
                break;
            default:
                throw new ValidationException("Media type not supported.");
        }
    }

    private static void validateDisplayOrders(List<MediaModel> mediaList)
    {
        int n = mediaList.size();
        boolean[] checked = new boolean[n];

        for (MediaModel media : mediaList)
        {
            short order = media.getDisplayOrder();
            if (order < 0 || order >= n)
            {
                throw new ValidationException("The media display order (displayOrder) is invalid.");
            }
            if (checked[order])
            {
                throw new ValidationException("The media display order (displayOrder) must be unique and free of conflicts.");
            }
            checked[order] = true;
        }
    }
}
