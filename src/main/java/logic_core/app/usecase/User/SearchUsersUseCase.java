package logic_core.app.usecase.User;


import logic_core.app.dto.request.SearchUsersRequest;
import logic_core.app.dto.response.UserSearchResponse;
import logic_core.app.dto.validator.UserValidator;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.SessionUserContext;
import logic_core.common.result.Result;
import logic_core.domain.model.UserModel;
import logic_core.domain.policy.UserPolicy;
import logic_core.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;


import java.util.List;
import java.util.UUID;



@RequiredArgsConstructor
public class SearchUsersUseCase
{
    private final AuthLockOrchestrator authLockOrchestrator;
    private final UserRepository userRepository;
    private final UserPolicy userPolicy;
    private final UserValidator validator;

    public Result<List<UserSearchResponse>> execute(SearchUsersRequest request
    )
    {

        try {

            validator.validateSearchUser(request.query(), request.page(), request.pageSize());

            SessionUserContext context = authLockOrchestrator.lockAndGetContextByToken(request.sessionToken());

            UUID actorId = context.lockedUser().getId();

            userPolicy.validateCanSearch(actorId);

            List<UserModel> users = userRepository.searchUsers(
                    actorId,
                    request.query(),
                    request.page(),
                    request.pageSize()
            );


            List<UserSearchResponse> response = users.stream()
                    .map(user -> new UserSearchResponse(user.getId(),
                                    user.getUsername(),
                                    user.getDisplayName(),
                                    user.getAvatarUrl())).toList();

            return Result.success(response);

        }
        catch(Exception e)
        {
            return Result.failure(e.getMessage());
        }
    }
}