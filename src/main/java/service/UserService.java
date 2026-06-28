package service;

import lombok.AllArgsConstructor;
import model.User;
import repository.FollowRepository;
import repository.UserRepository;
import util.PasswordHasher;

import java.util.Optional;

@AllArgsConstructor
public class UserService
{
    private final UserRepository userRepository;
    private final FollowRepository followRepository;


    public User register(String username, String email, String password)
    {
        validateRegistrationInput(username, email, password);

        if (userRepository.findByUsername(username).isPresent())
        {
            throw new IllegalArgumentException("Username already taken.");
        }

        User newUser = new User();
        newUser.setUsername(username.trim());
        newUser.setEmail(email.trim());
        newUser.setPasswordHash(PasswordHasher.hashPassword(password));

        return userRepository.save(newUser);
    }

    public Optional<User> login(String username, String password)
    {
        if (username == null || username.isBlank())
        {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (password == null || password.isBlank())
        {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        return userRepository.findByUsername(username.trim())
                .filter(user -> PasswordHasher.checkPassword(password, user.getPasswordHash()));
    }

    public void follow(Long followerId, Long followingId)
    {
        if (followerId == null || followingId == null)
        {
            throw new IllegalArgumentException("User IDs cannot be null.");
        }
        if (followerId.equals(followingId))
        {
            throw new IllegalArgumentException("User cannot follow themselves.");
        }


        followRepository.follow(followerId,followingId);

    }

    public void unfollow(Long followerId, Long followingId)
    {
        if (followerId == null || followingId == null)
        {
            throw new IllegalArgumentException("User IDs cannot be null.");
        }

        followRepository.unfollow(followerId, followingId);
    }

    private void validateRegistrationInput(String username, String email, String password)
    {
        if (username == null || username.isBlank())
        {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (email == null || email.isBlank())
        {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        if (password == null || password.isBlank())
        {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
    }
}
