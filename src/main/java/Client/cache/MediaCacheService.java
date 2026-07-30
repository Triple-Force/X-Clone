package Client.cache;

import Client.ClientDAOManager;
import Shared.Models.Media.Media;

import java.util.UUID;

public class MediaCacheService extends BaseCacheService<Media>
{
    public MediaCacheService()
    {
        super(ClientDAOManager.getInstance().getMediaDAO());
    }

    public Media find(UUID id)
    {
        return dao.findById(id);
    }
}