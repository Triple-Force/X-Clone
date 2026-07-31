package logic_core.app.usecase.follow;

import logic_core.app.dto.request.GetFollowersRequest;
import logic_core.app.dto.response.GetFollowersResponse;
import logic_core.app.dto.response.UserSummaryResponse;
import logic_core.app.mapper.UserSummaryResponseMapper;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.SessionUserContext;
import logic_core.common.exception.NotFoundException;
import logic_core.common.result.Result;
import logic_core.domain.model.FollowRelation;
import logic_core.domain.repository.RelationshipRepository;
import logic_core.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class GetFollowersUseCase
{

    @NonNull private final RelationshipRepository relationshipRepository;
    @NonNull private final UserRepository userRepository;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;


    public Result<GetFollowersResponse> execute(GetFollowersRequest request)
    {
        try
        {
            SessionUserContext context = lockOrchestrator.lockAndGetContextByToken(request.sessionToken());
            UUID currentUser = context.lockedUser().getId();
            if (request.targetId() == null)
            {
                return Result.failure(
                        "User id is required."
                );
            }

            if (!userRepository.existsById(request.targetId()))
            {
                throw new NotFoundException(
                        "User not found."
                );
            }

            List<FollowRelation> relations =
                    relationshipRepository.findByFollowingId(
                            request.targetId()
                    );

            List<UUID> followerIds =
                    relations.stream()
                            .map(FollowRelation::getFollowerId)
                            .toList();

            List<UserSummaryResponse> followers =
                    relations.stream()
                            .map(FollowRelation::getFollowerId)
                            .map(userRepository::findById)
                            .filter(java.util.Optional::isPresent)
                            .map(java.util.Optional::get)
                            .map(UserSummaryResponseMapper::toResponse)
                            .toList();


            return Result.success(
                    new GetFollowersResponse(
                            request.targetId(),
                            followers
                    )
            );

        }
        catch (NotFoundException e)
        {
            return Result.failure(
                    e.getMessage()
            );
        }
        catch (Exception e)
        {
            return Result.failure(
                    "Failed to get followers."
            );
        }
    }
}