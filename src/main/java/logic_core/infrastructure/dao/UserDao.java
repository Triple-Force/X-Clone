package logic_core.infrastructure.dao;

import Shared.Models.User.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import logic_core.domain.model.UserModel;
import logic_core.infrastructure.mapper.UserPersistenceMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserDao extends AbstractJpaDao<User>
{
    public UserDao(EntityManager entityManager)
    {
        super(entityManager, User.class);
    }

    public UserModel save(UserModel model)
    {
        try
        {
            User entity = UserPersistenceMapper.toPersistence(model);
            entityManager.persist(entity);
            entityManager.flush();
            return UserPersistenceMapper.toModel(entity);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }



    public Optional<User> findUserBySessionId(UUID sessionId)
    {
        List<User> users = entityManager.createQuery(
                        "SELECT s.user FROM Session s WHERE s.id = :sessionId",
                        User.class
                )
                .setParameter("sessionId", sessionId)
                .setMaxResults(1)
                .getResultList();

        return users.stream().findFirst();
    }

    public void deleteUser(User user)
    {
        remove(user);
    }

    public void updateUser(User updated)
    {
        merge(updated);
    }

    public Optional<User> findById(UUID id)
    {
        return entityManager.createQuery("""
            select u from User u
            where u.id = :userId
            """, User.class)
                .setParameter("userId", id)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultStream()
                .findFirst();
    }

    public Optional<User> findByIdForUpdate(UUID userId)
    {
        return entityManager.createQuery("""
            select u from User u
            where u.id = :userId
            """, User.class)
                .setParameter("userId", userId)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultStream()
                .findFirst();
    }


    public Optional<User> findByUsername(String username)
    {
        List<User> users = entityManager.createQuery(
                        "SELECT u FROM User u WHERE u.username = :username",
                        User.class
                )
                .setParameter("username", username)
                .setMaxResults(1)
                .getResultList();

        return users.stream().findFirst();
    }

    public Optional<User> findByEmail(String email)
    {
        List<User> users = entityManager.createQuery(
                        "SELECT u FROM User u WHERE u.email = :email",
                        User.class
                )
                .setParameter("email", email)
                .setMaxResults(1)
                .getResultList();

        return users.stream().findFirst();
    }

    public boolean existsByUsername(String username)
    {

        Long count = entityManager.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.username = :username",
                        Long.class
                )
                .setParameter("username", username)
                .getSingleResult();

        return count > 0;
    }

    public boolean existsByEmail(String email)
    {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.email = :email",
                        Long.class
                )
                .setParameter("email", email)
                .getSingleResult();

        System.out.println(count);

        return count > 0;
    }

    public boolean isActive(UserModel user)
    {
        List<Boolean> results = entityManager.createQuery(
                        "SELECT (u.isDeleted = true OR u.isActive = false) " +
                                "FROM User u WHERE u.id = :userId",
                        Boolean.class
                )
                .setParameter("userId", user.getId())
                .setMaxResults(1)
                .getResultList();

        return !results.stream().findFirst().orElse(false);
    }
}
