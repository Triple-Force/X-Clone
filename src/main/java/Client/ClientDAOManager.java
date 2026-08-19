package Client;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Conversation.Conversation;
import Shared.Models.HashtagFollow.HashtagFollow;
import Shared.Models.Media.Media;
import Shared.Models.Session.Session;
import Shared.Models.Tweet.Tweet;
import Shared.Models.User.User;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class ClientDAOManager
{
    private static final ClientDAOManager INSTANCE = new ClientDAOManager();

    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory(
            ClientCacheDatabase.PERSISTENCE_UNIT_NAME);
    private final Map<Class<?>, GenericDAO<?>> daoCache = new ConcurrentHashMap<>();

    private ClientDAOManager()
    {
    }

    public static ClientDAOManager getInstance()
    {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public <T> GenericDAO<T> getDao(Class<T> entityClass)
    {
        return (GenericDAO<T>) daoCache.computeIfAbsent(entityClass, c -> new GenericDAO<>(entityClass));
    }

    public GenericDAO<Conversation> getConversationDAO()
    {
        return getDao(Conversation.class);
    }

    public GenericDAO<HashtagFollow> getHashtagFollowDAO()
    {
        return getDao(HashtagFollow.class);
    }

    public GenericDAO<Media> getMediaDAO()
    {
        return getDao(Media.class);
    }

    public GenericDAO<Session> getSessionDAO()
    {
        return getDao(Session.class);
    }

    public GenericDAO<Tweet> getTweetDAO()
    {
        return getDao(Tweet.class);
    }

    public GenericDAO<User> getUserDAO()
    {
        return getDao(User.class);
    }
}
