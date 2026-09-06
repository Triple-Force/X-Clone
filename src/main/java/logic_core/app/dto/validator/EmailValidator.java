package logic_core.app.dto.validator;

import logic_core.common.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class EmailValidator
{
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    public void validate(String email)
    {
        if (email == null || email.isBlank())
        {
            throw new ValidationException("email.required");
        }

        String trimmed = email.trim();
        if (trimmed.length() > 100)
        {
            throw new ValidationException("Email cannot be longer than 100 characters.");
        }

        if (!EMAIL_PATTERN.matcher(trimmed).matches())
        {
            throw new ValidationException("Invalid email format.");
        }
    }
}