package logic_core.domain.policy;


import logic_core.domain.model.UserModel;
import logic_core.domain.repository.RelationshipRepository;
import logic_core.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;


@RequiredArgsConstructor
public class UserPolicy
{
    @NonNull private final RelationshipRepository relationshipRepository;
    @NonNull  private final UserRepository userRepository;

    public void validateCanSearch(UUID actorId)
    {

        if(!userRepository.existsById(actorId))
        {
            throw new RuntimeException("User does not exist.");
        }

    }

    public boolean canSeeUser(UUID actorId, UUID targetId)
    {

        if(relationshipRepository.isBlockedBy(actorId, targetId))
        {
            return false;
        }

        return true;
    }

    public void validateCanUpdateProfile(UserModel user)
    {
        if(!user.isActive())
        {
            throw new RuntimeException("User is not active.");
        }
    }
}