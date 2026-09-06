package logic_core.app.usecase.User;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.GetIsFollowingRequest;
import logic_core.app.dto.response.GetIsFollowingResponse;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.common.result.Result;
import logic_core.domain.model.UserModel;
import logic_core.domain.repository.RelationshipRepository;
import logic_core.domain.repository.TweetRepository;
import logic_core.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetIsFollowingUseCase {

    @NonNull
    private final UserRepository userRepository;
    @NonNull
    private final RelationshipRepository relationshipRepository;
    @NonNull
    private final AuthLockOrchestrator lockOrchestrator;


    @Transactional
    public Result<GetIsFollowingResponse> execute(GetIsFollowingRequest request) {
        try {
            UserModel currentUser =
                    lockOrchestrator
                            .lockAndGetContextByToken(request.sessionToken())
                            .lockedUser();

            if (userRepository.findById(request.targetUserId()).isEmpty()) {
                return Result.failure("User not found");
            }

            boolean following =
                    relationshipRepository.existsFollowRelation(
                            currentUser.getId(),
                            request.targetUserId()
                    );

            return Result.success(
                    GetIsFollowingResponse.builder()
                            .following(following)
                            .build()
            );
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}