package logic_core.app.usecase.media;

import logic_core.app.dto.request.DownloadMediaRequest;
import logic_core.app.dto.response.DownloadMediaResponse;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.domain.model.MediaModel;
import logic_core.domain.repository.MediaRepository;
import logic_core.common.exception.NotFoundException;
import logic_core.common.result.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DownloadMediaUseCase
{

    @NonNull private final MediaRepository mediaRepository;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;


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

            // Media is attached to public tweets, so any authenticated user may
            // fetch it, but an unauthenticated caller must not be able to query
            // media metadata by id (prevents anonymous IDOR-style enumeration).
            lockOrchestrator.lockAndGetContextByToken(
                    request.sessionToken()
            );

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