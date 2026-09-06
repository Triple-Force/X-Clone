package logic_core.domain.policy;


import logic_core.domain.model.UserModel;
import logic_core.domain.repository.RelationshipRepository;
import logic_core.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserPolicy
{
    private final RelationshipRepository relationshipRepository;
    private final UserRepository userRepository;

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