package Server;

import Shared.Database.DAO.GenericDAO;
import Shared.Database.Database;
import Shared.Models.Block.Block;
import Shared.Models.Conversation.Conversation;
import Shared.Models.ConversationMember.ConversationMember;
import Shared.Models.DirectMessage.DirectMessage;
import Shared.Models.Follow.Follow;
import Shared.Models.Hashtag.Hashtag;
import Shared.Models.HashtagFollow.HashtagFollow;
import Shared.Models.Like.Like;
import Shared.Models.Media.Media;
import Shared.Models.Mute.Mute;
import Shared.Models.Notification.Notification;
import Shared.Models.Poll.Poll;
import Shared.Models.PollOption.PollOption;
import Shared.Models.PollVote.PollVote;
import Shared.Models.Session.Session;
import Shared.Models.Tweet.Tweet;
import Shared.Models.TweetEdit.TweetEdit;
import Shared.Models.TweetHashtag.TweetHashtag;
import Shared.Models.TweetMention.TweetMention;
import Shared.Models.User.User;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class ServerDAOManager
{
    private static final ServerDAOManager INSTANCE = new ServerDAOManager();

    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory(Database.PERSISTENCE_UNIT_NAME);
    private final Map<Class<?>, GenericDAO<?>> daoCache = new ConcurrentHashMap<>();

    private ServerDAOManager()
    {
    }

    public static ServerDAOManager getInstance()
    {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public <T> GenericDAO<T> getDao(Class<T> entityClass)
    {
        return (GenericDAO<T>) daoCache.computeIfAbsent(entityClass, c -> new GenericDAO<>(entityClass));
    }

    public GenericDAO<Block> getBlockDAO()
    {
        return getDao(Block.class);
    }

    public GenericDAO<Conversation> getConversationDAO()
    {
        return getDao(Conversation.class);
    }

    public GenericDAO<ConversationMember> getConversationMemberDAO()
    {
        return getDao(ConversationMember.class);
    }

    public GenericDAO<DirectMessage> getDirectMessageDAO()
    {
        return getDao(DirectMessage.class);
    }

    public GenericDAO<Follow> getFollowDAO()
    {
        return getDao(Follow.class);
    }

    public GenericDAO<Hashtag> getHashtagDAO()
    {
        return getDao(Hashtag.class);
    }

    public GenericDAO<HashtagFollow> getHashtagFollowDAO()
    {
        return getDao(HashtagFollow.class);
    }

    public GenericDAO<Like> getLikeDAO()
    {
        return getDao(Like.class);
    }

    public GenericDAO<Media> getMediaDAO()
    {
        return getDao(Media.class);
    }

    public GenericDAO<Mute> getMuteDAO()
    {
        return getDao(Mute.class);
    }

    public GenericDAO<Notification> getNotificationDAO()
    {
        return getDao(Notification.class);
    }

    public GenericDAO<Poll> getPollDAO()
    {
        return getDao(Poll.class);
    }

    public GenericDAO<PollOption> getPollOptionDAO()
    {
        return getDao(PollOption.class);
    }

    public GenericDAO<PollVote> getPollVoteDAO()
    {
        return getDao(PollVote.class);
    }

    public GenericDAO<Session> getSessionDAO()
    {
        return getDao(Session.class);
    }

    public GenericDAO<Tweet> getTweetDAO()
    {
        return getDao(Tweet.class);
    }

    public GenericDAO<TweetEdit> getTweetEditDAO()
    {
        return getDao(TweetEdit.class);
    }

    public GenericDAO<TweetHashtag> getTweetHashtagDAO()
    {
        return getDao(TweetHashtag.class);
    }

    public GenericDAO<TweetMention> getTweetMentionDAO()
    {
        return getDao(TweetMention.class);
    }

    public GenericDAO<User> getUserDAO()
    {
        return getDao(User.class);
    }
}