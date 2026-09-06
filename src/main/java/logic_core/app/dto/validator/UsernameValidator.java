package logic_core.app.dto.validator;

import logic_core.common.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class UsernameValidator
{
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");

    public void validate(String username)
    {
        if (username == null || username.isBlank())
        {
            throw new ValidationException("Username cannot be empty.");
        }

        String trimmed = username.trim();

        if (!USERNAME_PATTERN.matcher(trimmed).matches())
        {
            throw new ValidationException(
                    "Username must be between 3 and 20 characters and contain only letters, numbers, underscores, or hyphens."
            );
        }
    }
}