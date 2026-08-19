package logic_core.app.usecase.media;

import logic_core.app.dto.request.DownloadMediaRequest;
import logic_core.app.dto.response.DownloadMediaResponse;
import logic_core.domain.model.MediaModel;
import logic_core.domain.repository.MediaRepository;
import logic_core.common.exception.NotFoundException;
import logic_core.common.result.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;


@RequiredArgsConstructor
public class DownloadMediaUseCase
{

    @NonNull
    private final MediaRepository mediaRepository;


    public Result<DownloadMediaResponse> execute(DownloadMediaRequest request)
    {
        try
        {
            if (request == null)
            {
                return Result.failure(
                        "Media id is required."
                );
            }



            MediaModel media =
                    mediaRepository.findById(request.mediaId())
                            .orElseThrow(() ->
                                    new NotFoundException(
                                            "Media not found."
                                    )
                            );


            DownloadMediaResponse response =
                    new DownloadMediaResponse(
                            media.getMediaId(),
                            media.getMediaUrl(),
                            media.getOriginalFilename(),
                            media.getFileSizeBytes(),
                            media.getMediaType()
                    );


            return Result.success(response);

        }
        catch (NotFoundException e)
        {
            return Result.failure(e.getMessage());
        }
        catch (Exception e)
        {
            return Result.failure(
                    "Failed to download media."
            );
        }
    }
}