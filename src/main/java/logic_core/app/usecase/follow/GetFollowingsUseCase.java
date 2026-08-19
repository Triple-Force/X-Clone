package logic_core.app.usecase.follow;

import logic_core.app.dto.request.GetFollowingsRequest;
import logic_core.app.dto.response.FollowingsResponse;
import logic_core.app.dto.response.UserSummaryResponse;
import logic_core.app.mapper.UserSummaryResponseMapper;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.SessionUserContext;
import logic_core.common.result.Result;
import logic_core.domain.model.FollowRelation;
import logic_core.domain.repository.RelationshipRepository;
import logic_core.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class GetFollowingsUseCase
{
    @NonNull private final RelationshipRepository relationshipRepository;
    @NonNull private final UserRepository userRepository;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    public Result<FollowingsResponse> execute(@NonNull GetFollowingsRequest request)
    {
        try
        {
            SessionUserContext context = lockOrchestrator.lockAndGetContextByToken(request.sessionToken());

            UUID currentUserId = context.lockedUser().getId();

            UUID targetUserId = request.userId() != null ? request.userId() : currentUserId;

            List<FollowRelation> relations = relationshipRepository.findByFollowerId(targetUserId);

            List<UUID> followingIds =
                    relations.stream()
                            .map(FollowRelation::getFollowingId)
                            .toList();

            List<UserSummaryResponse> users =
                    followingIds.stream()
                            .map(id ->
                                    userRepository.findById(id)
                                            .map(UserSummaryResponseMapper::toResponse)
                                            .orElse(null)
                            )
                            .filter(user -> user != null)
                            .toList();

            return Result.success(new FollowingsResponse(users));

        }
        catch (Exception e)
        {
            return Result.failure("Failed to get followings.");
        }
    }
}