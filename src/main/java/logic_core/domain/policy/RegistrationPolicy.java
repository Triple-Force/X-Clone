package logic_core.domain.policy;

import logic_core.common.exception.ConflictException;
import logic_core.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public class RegistrationPolicy
{
    @NonNull
    private final UserRepository userRepository;

    public void validate(String username, String email)
    {
        requireNonBlank(username, "username");
        requireNonBlank(email, "email");

        validateUsernameIsAvailable(username);
        validateEmailIsAvailable(email);
    }

    private void validateUsernameIsAvailable(String username)
    {
        requireNonBlank(username, "username");

        if (userRepository.existsByUsername(username))
        {
            throw new ConflictException("Username is already taken.");
        }
    }

    private void validateEmailIsAvailable(String email)
    {
        requireNonBlank(email, "email");

        if (userRepository.existsByEmail(email))
        {
            throw new ConflictException("Email is already registered.");
        }
    }

    private void requireNonBlank(String value, String fieldName)
    {
        Objects.requireNonNull(value, fieldName + " must not be null");

        if (value.isBlank())
        {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}