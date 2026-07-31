package logic_core.app.facade;

import logic_core.app.dto.request.DeleteMediaRequest;
import logic_core.app.dto.request.DownloadMediaRequest;
import logic_core.app.dto.response.DownloadMediaResponse;
import logic_core.app.usecase.media.DeleteMediaUseCase;
import logic_core.app.usecase.media.DownloadMediaUseCase;
import logic_core.common.result.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MediaFacade
{
    @NonNull private final DeleteMediaUseCase deleteMediaUseCase;
    @NonNull private final DownloadMediaUseCase downloadMediaUseCase;

    public Result<Void> deleteMedia(DeleteMediaRequest request)
    {
        return deleteMediaUseCase.execute(request);
    }

    public Result<DownloadMediaResponse> downloadMedia(DownloadMediaRequest request)
    {
        return downloadMediaUseCase.execute(request);
    }

}
