package logic_core.infrastructure.repository;

import Shared.Models.User.User;
import jakarta.persistence.EntityManager;
import logic_core.domain.model.UserModel;
import logic_core.domain.repository.UserRepository;
import logic_core.infrastructure.dao.UserDao;
import logic_core.infrastructure.mapper.UserPersistenceMapper;

import java.util.*;

public class JpaUserRepository implements UserRepository
{
    private final UserDao userDao;
    private final EntityManager em;

    public JpaUserRepository(UserDao userDao, EntityManager em)
    {
        this.userDao = Objects.requireNonNull(userDao, "userDao must not be null");
        this.em = em;
    }

    @Override
    public Optional<UserModel> save(UserModel model)
    {
        User user = UserPersistenceMapper.toPersistence(model);
        return Optional.ofNullable(UserPersistenceMapper.toModel(userDao.save(user)));
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
        return userDao.isActive(model.getId());
    }

    @Override
    public Optional<UserModel> findByIdForUpdate(UUID userId)
    {
        return userDao.findByIdForUpdate(userId)
                .map(UserPersistenceMapper::toModel);
    }

    public Optional<UserModel> findByUsernameForUpdate(String username)
    {
        return userDao.findByUsernameForUpdate(username)
                .map(UserPersistenceMapper::toModel);
    }

    @Override
    public List<UserModel> searchUsers(UUID actorId, String query, int page, int pageSize)
    {
        List<User> users = userDao.searchUsers(actorId,query,page, pageSize);

        List<UserModel> userModels = new ArrayList<>(users.size());

        for (User user : users)
        {
            userModels.add(UserPersistenceMapper.toModel(user));
        }

        return userModels;
    }

    @Override
    public Optional<UserModel> findAuthorByTweetId(UUID tweetId)
    {
        return userDao.findAuthorByTweetId(tweetId)
                .map(UserPersistenceMapper::toModel);
    }
}
