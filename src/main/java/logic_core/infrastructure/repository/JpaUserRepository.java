package logic_core.infrastructure.repository;

import Shared.Models.User.User;
import logic_core.domain.model.UserModel;
import logic_core.domain.repository.UserRepository;
import logic_core.infrastructure.dao.UserDao;
import logic_core.infrastructure.mapper.UserPersistenceMapper;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class JpaUserRepository implements UserRepository
{
    private final UserDao userDao;

    public JpaUserRepository(UserDao userDao)
    {
        this.userDao = Objects.requireNonNull(userDao, "userDao must not be null");
    }

    @Override
    public Optional<UserModel> save(UserModel model)
    {
        return Optional.ofNullable(userDao.save(model));
    }

    @Override
    public void update(UserModel model)
    {
        userDao.findById(model.getId()).ifPresent(user -> {
            UserPersistenceMapper.updateEntity(user, model);
            userDao.updateUser(user);
        });
    }

    @Override
    public Optional<UserModel> findById(UUID userId)
    {
        return userDao.findById(Objects.requireNonNull(userId))
                .map(UserPersistenceMapper::toModel);
    }

    @Override
    public Optional<UserModel> findByUsername(String username)
    {
        return userDao.findByUsername(Objects.requireNonNull(username))
                .map(UserPersistenceMapper::toModel);
    }

    @Override
    public Optional<UserModel> findByEmail(String email)
    {
        return userDao.findByEmail(Objects.requireNonNull(email))
                .map(UserPersistenceMapper::toModel);
    }

    @Override
    public boolean existsByUsername(String username)
    {
        return userDao.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email)
    {
        System.out.println(email);
        return userDao.existsByEmail(email);
    }

    @Override
    public boolean existsById(UUID userId)
    {

        return userDao.findById(userId).isPresent();
    }

    @Override
    public void delete(UserModel model)
    {
        userDao.findById(model.getId()).ifPresent(user -> {
            user.setDeleted(true);
            userDao.updateUser(user);
        });
    }

    public Optional<UserModel> findUserBySessionId(UUID sessionId)
    {

        return userDao.findUserBySessionId(sessionId)
                .map(UserPersistenceMapper::toModel);
    }

    public boolean isActive(UserModel model)
    {
        return userDao.isActive(model);
    }

    @Override
    public Optional<User> findByIdForUpdate(UUID userId)
    {
        return userDao.findByIdForUpdate(userId);
    }
}
