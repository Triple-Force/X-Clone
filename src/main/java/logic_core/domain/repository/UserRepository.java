package logic_core.domain.repository;

import Shared.Models.User.User;
import logic_core.domain.model.UserModel;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository
{
    Optional<UserModel> save(UserModel user);

    void update(UserModel user);

    void delete(UserModel user);

    Optional<UserModel> findById(UUID userId);

    Optional<User> findByIdForUpdate(UUID userId);

    Optional<UserModel> findByUsername(String username);

    Optional<UserModel> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsById(UUID userId);

    Optional<UserModel> findUserBySessionId(UUID sessionId);

    boolean isActive(UserModel user);
}
