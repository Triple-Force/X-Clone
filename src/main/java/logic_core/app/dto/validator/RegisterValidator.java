package logic_core.app.dto.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterValidator
{
    private final UsernameValidator usernameValidator;
    private final PasswordValidator passwordValidator;
    private final EmailValidator emailValidator;

    public void validate(String username, String password, String email)
    {
        usernameValidator.validate(username);
        passwordValidator.validate(password);
        emailValidator.validate(email);
    }
}