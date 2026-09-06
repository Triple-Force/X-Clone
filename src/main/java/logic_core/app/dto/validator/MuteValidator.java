package logic_core.app.dto.validator;

import logic_core.common.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class MuteValidator
{
    public void validate(UUID muterId, UUID mutedId)
    {
        Objects.requireNonNull(muterId, "muterId must not be null.");
        Objects.requireNonNull(mutedId, "mutedId must not be null.");

        if (muterId.equals(mutedId))
        {
            throw new ValidationException("Users cannot mute themselves.");
        }
    }
}
