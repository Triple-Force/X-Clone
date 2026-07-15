package logic_core.app.dto.validator;

public class RegisterValidator
{

    public void validate(String username, String password, String email)
    {
        UsernameValidator.validate(username);
        PasswordValidator.validate(password);
        EmailValidator.validate(email);
    }
}
