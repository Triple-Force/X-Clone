package logic_core.app.dto.validator;

import logic_core.common.exception.ValidationException;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class BlockValidator
{
    public void validate(UUID blockerId, UUID blockedId)
    {
        Objects.requireNonNull(blockerId, "BlockerId must not be null");
        Objects.requireNonNull(blockedId, "BlockedId must not be null");

        if (blockerId.equals(blockedId))
        {
            throw new ValidationException("Users cannot block themselves.");
        }
    }
}
