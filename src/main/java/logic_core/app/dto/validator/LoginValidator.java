package logic_core.app.dto.validator;

public class LoginValidator
{
    public void validate(String username, String password)
    {
        UsernameValidator.validate(username);
        PasswordValidator.validate(password);
    }

}