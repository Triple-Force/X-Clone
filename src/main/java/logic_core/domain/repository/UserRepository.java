package logic_core.domain.repository;

import logic_core.domain.model.UserModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository
{
    Optional<UserModel> save(UserModel user);

    void update(UserModel user);

    void delete(UserModel user);

    Optional<UserModel> findById(UUID userId);

    Optional<UserModel> findByIdForUpdate(UUID userId);

    Optional<UserModel> findByUsername(String username);

    Optional<UserModel> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsById(UUID userId);

    Optional<UserModel> findUserBySessionId(UUID sessionId);

    boolean isActive(UserModel user);

//    Optional<UserModel> findAuthorByMediaId(UUID mediaId);

Optional<UserModel> findByUsernameForUpdate(String username);

    List<UserModel> searchUsers(UUID actorId, String query, int limit, int pageSize);
}
