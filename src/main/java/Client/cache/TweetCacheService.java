package Client.cache;

import Client.ClientDAOManager;
import Shared.Models.Tweet.Tweet;

import java.util.UUID;

public class TweetCacheService extends BaseCacheService<Tweet>
{
    public TweetCacheService()
    {
        super(ClientDAOManager.getInstance().getTweetDAO());
    }

    public Tweet find(UUID id)
    {
        return dao.findById(id);
    }
}