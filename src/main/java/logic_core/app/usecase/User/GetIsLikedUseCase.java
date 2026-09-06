package logic_core.app.usecase.User;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.GetIsLikedRequest;
import logic_core.app.dto.response.GetIsLikedResponse;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.common.result.Result;
import logic_core.domain.model.UserModel;
import logic_core.domain.repository.RelationshipRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetIsLikedUseCase {

    @NonNull
    private final RelationshipRepository likeRepository;

    @NonNull
    private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<GetIsLikedResponse> execute(GetIsLikedRequest request) {

        try {

            UserModel currentUser = lockOrchestrator
                    .lockAndGetContextByToken(request.sessionToken()).lockedUser();

            boolean liked = likeRepository.existsLikeRelation(
                    currentUser.getId(),
                    request.tweetId()
            );

            return Result.success(
                    GetIsLikedResponse.builder()
                            .liked(liked)
                            .build()
            );

        } catch (Exception e) {

            return Result.failure(e.getMessage());
        }
    }
}