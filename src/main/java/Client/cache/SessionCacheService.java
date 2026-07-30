package Client.cache;

import Client.ClientDAOManager;
import Shared.Models.Session.Session;

import java.util.UUID;

public class SessionCacheService extends BaseCacheService<Session>
{
    public SessionCacheService()
    {
        super(ClientDAOManager.getInstance().getSessionDAO());
    }

    public Session find(UUID id)
    {
        return dao.findById(id);
    }

    public Session findByToken(String token)
    {
        return dao.findByField("token", token);
    }
}