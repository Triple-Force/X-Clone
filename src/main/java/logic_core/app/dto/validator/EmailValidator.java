package logic_core.app.dto.validator;

import logic_core.common.exception.ValidationException;

import java.util.regex.Pattern;

public class EmailValidator
{
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    public static void validate(String email)
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
