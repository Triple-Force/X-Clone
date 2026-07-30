package Client.cache;

import lombok.Getter;

@Getter
public class ClientCacheService
{
    private final UserCacheService userCacheService;
    private final TweetCacheService tweetCacheService;
    private final ConversationCacheService conversationCacheService;
    private final MediaCacheService mediaCacheService;
    private final SessionCacheService sessionCacheService;

    public ClientCacheService()
    {
        this.userCacheService = new UserCacheService();
        this.tweetCacheService = new TweetCacheService();
        this.conversationCacheService = new ConversationCacheService();
        this.mediaCacheService = new MediaCacheService();
        this.sessionCacheService = new SessionCacheService();
    }
}