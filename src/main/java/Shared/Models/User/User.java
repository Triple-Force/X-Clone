package Shared.Models.User;

import Shared.Models.Block.Block;
import Shared.Models.ConversationMember.ConversationMember;
import Shared.Models.DirectMessage.DirectMessage;
import Shared.Models.Follow.Follow;
import Shared.Models.HashtagFollow.HashtagFollow;
import Shared.Models.Like.Like;
import Shared.Models.MutableEntity;
import Shared.Models.Mute.Mute;
import Shared.Models.Notification.Notification;
import Shared.Models.PollVote.PollVote;
import Shared.Models.Session.Session;
import Shared.Models.Tweet.Tweet;
import Shared.Models.TweetMention.TweetMention;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.ColumnDefault;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", indexes = {@Index(name = "idx_users_is_deleted", columnList = "is_deleted")})
public class User extends MutableEntity
{
    @Column(name = "username", unique = true, length = 50)
    private String username;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "bio", length = 160)
    private String bio;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "banner_url")
    private String bannerUrl;

    @Column(name = "is_verified", nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private boolean isVerified = false;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("true")
    @Builder.Default
    private boolean isActive = true;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Session> sessions;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<Tweet> tweets;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "following", fetch = FetchType.LAZY)
    private List<Follow> followers;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "follower", fetch = FetchType.LAZY)
    private List<Follow> following;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Like> likes;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<HashtagFollow> followedHashtags;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "blocker", fetch = FetchType.LAZY)
    private List<Block> blocksMade;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "blocked", fetch = FetchType.LAZY)
    private List<Block> blocksReceived;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "muter", fetch = FetchType.LAZY)
    private List<Mute> mutesMade;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "muted", fetch = FetchType.LAZY)
    private List<Mute> mutesReceived;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<ConversationMember> conversationMemberships;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY)
    private List<DirectMessage> sentMessages;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "recipient", fetch = FetchType.LAZY)
    private List<Notification> notificationsReceived;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "actor", fetch = FetchType.LAZY)
    private List<Notification> notificationsTriggered;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<PollVote> pollVotes;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "mentionedUser", fetch = FetchType.LAZY)
    private List<TweetMention> mentions;

    @Override
    public void redact()
    {
        this.username = null;
        this.email = null;
        this.passwordHash = null;
        this.displayName = "Deleted User";
        this.bio = null;
        this.avatarUrl = null;
        this.bannerUrl = null;
        this.isActive = false;
    }

    @Override
    public void onSoftDelete(EntityManager em)
    {
        List<Tweet> tweets = em.createQuery("SELECT t FROM Tweet t WHERE t.author = :author AND t.isDeleted = false",
                Tweet.class).setParameter("author", this).getResultList();

        for (Tweet tweet : tweets)
        {
            tweet.setDeleted(true);
            tweet.redact();
            tweet.onSoftDelete(em);
            em.merge(tweet);
        }

        hardDeleteWhere(em, Session.class, "user", this);

        hardDeleteWhere(em, Follow.class, "follower", this);
        hardDeleteWhere(em, Follow.class, "following", this);

        hardDeleteWhere(em, Like.class, "user", this);
        hardDeleteWhere(em, HashtagFollow.class, "user", this);
        hardDeleteWhere(em, PollVote.class, "user", this);

        hardDeleteWhere(em, Block.class, "blocker", this);
        hardDeleteWhere(em, Block.class, "blocked", this);

        hardDeleteWhere(em, Mute.class, "muter", this);
        hardDeleteWhere(em, Mute.class, "muted", this);

        hardDeleteWhere(em, ConversationMember.class, "user", this);
        hardDeleteWhere(em, TweetMention.class, "mentionedUser", this);

        hardDeleteWhere(em, Notification.class, "recipient", this);
        hardDeleteWhere(em, Notification.class, "actor", this);
    }
}
