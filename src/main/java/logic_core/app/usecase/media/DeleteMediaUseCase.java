package logic_core.app.usecase.media;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.DeleteMediaRequest;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.SessionUserContext;
import logic_core.common.exception.ForbiddenException;
import logic_core.common.exception.NotFoundException;
import logic_core.common.result.Result;
import logic_core.domain.model.MediaModel;
import logic_core.domain.model.TweetModel;
import logic_core.domain.repository.MediaRepository;
import logic_core.domain.repository.TweetRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteMediaUseCase
{
    @NonNull private final MediaRepository mediaRepository;
    @NonNull private final TweetRepository tweetRepository;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<Void> execute(DeleteMediaRequest request)
    {
        try
        {
            if (request == null || request.mediaId() == null)
            {
                return Result.failure(
                        "Media id is required."
                );
            }


            SessionUserContext context =
                    lockOrchestrator.lockAndGetContextByToken(
                            request.sessionToken()
                    );


            UUID currentUserId =
                    context.lockedUser().getId();



            MediaModel media =
                    mediaRepository.findById(
                                    request.mediaId()
                            )
                            .orElseThrow(() ->
                                    new NotFoundException(
                                            "Media not found."
                                    )
                            );



            TweetModel tweet =
                    tweetRepository.findById(
                                    media.getTweetId()
                            )
                            .orElseThrow(() ->
                                    new NotFoundException(
                                            "Tweet not found."
                                    )
                            );



            if (!tweet.getAuthorId().equals(currentUserId))
            {
                throw new ForbiddenException(
                        "You cannot delete media from another user's tweet."
                );
            }



            mediaRepository.delete(
                    media.getMediaId()
            );


            return Result.success(null);

        }
        catch (NotFoundException |
               ForbiddenException e)
        {
            return Result.failure(
                    e.getMessage()
            );
        }
        catch (Exception e)
        {
            return Result.failure(
                    "Failed to delete media."
            );
        }
    }
}