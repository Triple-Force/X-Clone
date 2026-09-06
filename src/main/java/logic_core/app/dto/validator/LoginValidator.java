package logic_core.app.dto.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginValidator
{
    private final UsernameValidator usernameValidator;
    private final PasswordValidator passwordValidator;

    public void validate(String username, String password)
    {
        usernameValidator.validate(username);
        passwordValidator.validate(password);
    }

}