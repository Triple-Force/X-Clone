package Client.cache;

import Client.ClientDAOManager;
import Shared.Models.Conversation.Conversation;

import java.util.UUID;

public class ConversationCacheService extends BaseCacheService<Conversation>
{
    public ConversationCacheService()
    {
        super(ClientDAOManager.getInstance().getConversationDAO());
    }

    public Conversation find(UUID id)
    {
        return dao.findById(id);
    }
}