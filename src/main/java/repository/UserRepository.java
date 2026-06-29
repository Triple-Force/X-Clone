package repository;


import model.User;

import java.util.List;
import java.util.Optional;


public interface UserRepository
{
    // --- create or update user ---
    User save(User user);

    // find by primary key
    Optional<User> findById(Long id);

    // --- authentication & search ---
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    // --- validation ---
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // --- utility ---
    List<User> findAll();
}
