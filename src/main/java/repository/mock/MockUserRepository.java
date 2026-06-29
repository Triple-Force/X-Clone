package repository.mock;

import model.User;
import repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MockUserRepository implements UserRepository
{
    //Simulation
    private final List<User> users = new ArrayList<>();
    private long currentId = 1;

    @Override
    public User save(User user)
    {
        if (user.getId() == null)
        {
            user.setId(currentId++);
            users.add(user);
        }
        else
        {
            users.removeIf(u -> u.getId().equals(user.getId()));
            users.add(user);
        }
        return user;
    }

    @Override
    public Optional<User> findById(Long userId)
    {
        return users.stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email)
    {

        for (User u : users)
        {
            if (u.getEmail().equalsIgnoreCase(email))
            {
                return Optional.of(u);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username)
    {
        return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public boolean existsByUsername(String username)
    {
        return users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
    }

    @Override
    public boolean existsByEmail(String email)
    {
        return users.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public List<User> findAll()
    {
        return new ArrayList<>(users);
    }
}

