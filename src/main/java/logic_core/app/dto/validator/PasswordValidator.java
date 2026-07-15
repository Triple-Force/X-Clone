package logic_core.app.dto.validator;

import logic_core.common.exception.ValidationException;

import java.awt.*;

public class PasswordValidator
{
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 64;

    public static void validate(String password)
    {
        if (password == null || password.isEmpty())
        {
            throw  new ValidationException("Password cannot be empty.");
        }

        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH)
        {
            throw  new ValidationException(
                    String.format("Password must be between %d and %d characters long.", MIN_LENGTH, MAX_LENGTH)
            );
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray())
        {
            if (Character.isUpperCase(c)) hasUppercase = true;
            else if (Character.isLowerCase(c)) hasLowercase = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }

        if (!hasUppercase || !hasLowercase || !hasDigit)
        {
            throw new ValidationException(
                    "Password must contain at least one uppercase letter, one lowercase letter, and one digit."
            );
        }
    }
}
