package Client.cache;

import Client.ClientDAOManager;
import Client.cache.BaseCacheService;
import Shared.Models.User.User;

import java.util.UUID;

public class UserCacheService extends BaseCacheService<User>
{
    public UserCacheService()
    {
        super(ClientDAOManager.getInstance().getUserDAO());
    }

    public User find(UUID id)
    {
        return dao.findById(id);
    }

    public User findByUsername(String username)
    {
        return dao.findByField("username", username);
    }
}