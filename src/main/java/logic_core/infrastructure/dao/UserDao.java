package logic_core.infrastructure.dao;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.User.User;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserDao extends GenericDAO<User>
{
    public UserDao()
    {
        super(User.class);
    }

    public User save(User model)
    {
        insert(model);
        return model;
    }

    public Optional<User> findUserBySessionId(UUID sessionId)
    {
        return Optional.ofNullable(
                findOneByJpql(
                        """
                        SELECT s.user
                        FROM Session s
                        WHERE s.id = :sessionId
                        AND s.user.isDeleted = false
                        """,
                        q -> q.setParameter("sessionId", sessionId)
                )
        );
    }

    public void deleteUser(User user)
    {
        delete(user);
    }

    public void updateUser(User updated)
    {
        update(updated);
    }

    public Optional<User> findById(UUID id)
    {
        return Optional.ofNullable(
                findOneByJpql(
                        """
                        SELECT u
                        FROM User u
                        WHERE u.id = :id
                        AND u.isDeleted = false
                        """,
                        q -> q.setParameter("id", id)
                )
        );
    }

    public Optional<User> findByIdForUpdate(UUID userId)
    {
        return Optional.ofNullable(
                findOneByJpql(
                        "SELECT u FROM User u WHERE u.id = :userId AND u.isDeleted = false",
                        q -> q.setParameter("userId", userId)
                                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                )
        );
    }

    public Optional<User> findByUsername(String username)
    {
        return Optional.ofNullable(
                findOneByJpql(
                        """
                        SELECT u
                        FROM User u
                        WHERE u.username = :username
                        AND u.isDeleted = false
                        """,
                        q -> q.setParameter("username", username)
                )
        );
    }

    public Optional<User> findByUsernameForUpdate(String username)
    {
        return Optional.ofNullable(
                findOneByJpql(
                        "SELECT u FROM User u WHERE u.username = :username AND u.isDeleted = false",
                        q -> q.setParameter("username", username)
                                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                )
        );
    }


    public Optional<User> findByEmail(String email)
    {
        return Optional.ofNullable(
                findOneByJpql(
                        """
                        SELECT u
                        FROM User u
                        WHERE u.email = :email
                        AND u.isDeleted = false
                        """,
                        q -> q.setParameter("email", email)
                )
        );
    }

    public boolean existsByUsername(String username)
    {
        return countByJpql(
                """
                SELECT COUNT(u)
                FROM User u
                WHERE u.username = :username
                AND u.isDeleted = false
                """,
                q -> q.setParameter("username", username)
        ) > 0;
    }

    public boolean existsByEmail(String email)
    {
        return countByJpql(
                """
                SELECT COUNT(u)
                FROM User u
                WHERE u.email = :email
                AND u.isDeleted = false
                """,
                q -> q.setParameter("email", email)
        ) > 0;
    }

    public boolean isActive(UUID userId)
    {
        return countByJpql(
                """
                SELECT COUNT(u)
                FROM User u
                WHERE u.id = :userId
                AND u.isDeleted = false
                AND u.isActive = true
                """,
                q -> q.setParameter("userId", userId)
        ) > 0;
    }

    public Optional<User> findAuthorByTweetId(UUID tweetId)
    {
        return Optional.ofNullable(
                findOneByJpql(
                        """
                        SELECT t.author
                        FROM Tweet t
                        WHERE t.id = :tweetId
                        AND t.isDeleted = false
                        AND t.author.isDeleted = false
                        """,
                        q -> q.setParameter("tweetId", tweetId)
                )
        );
    }

    public List<User> searchUsers(UUID actorId, String query, int page, int pageSize)
    {
        int offset = page * pageSize;

        return findByJpql("""
        SELECT u
        FROM User u
        WHERE u.isDeleted = false
          AND u.id <> :actorId
          AND (
                u.username LIKE :keyword
             OR u.displayName LIKE :keyword
          )
        ORDER BY u.username ASC
        """,
                q -> q.setParameter("actorId", actorId)
                        .setParameter("keyword", "%" + query + "%")
        );
    }
}
