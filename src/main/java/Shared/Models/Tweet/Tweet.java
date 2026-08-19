package Shared.Models.Tweet;

import Shared.Models.Like.Like;
import Shared.Models.Media.Media;
import Shared.Models.MutableEntity;
import Shared.Models.Notification.Notification;
import Shared.Models.Poll.Poll;
import Shared.Models.TweetEdit.TweetEdit;
import Shared.Models.TweetHashtag.TweetHashtag;
import Shared.Models.TweetMention.TweetMention;
import Shared.Models.User.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tweets",
        indexes = {@Index(name = "idx_tweets_author_id_published_at", columnList = "author_id, published_at"), @Index(
                name = "idx_tweets_reply_to_id", columnList = "reply_to_id"), @Index(name = "idx_tweets_retweet_of_id",
                columnList = "retweet_of_id"), @Index(name = "idx_tweets_quote_of_id",
                columnList = "quote_of_id"), @Index(name = "idx_tweets_published_at",
                columnList = "published_at"), @Index(name = "idx_tweets_is_deleted", columnList = "is_deleted")})
public class Tweet extends MutableEntity
{
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User author;

    @Column(name = "content", length = 280)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Tweet repliedToTweet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retweet_of_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Tweet retweetedTweet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_of_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Tweet quotedTweet;

    @Column(name = "is_pinned", nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private boolean isPinned = false;

    @Column(name = "scheduled_at")
    private OffsetDateTime scheduledAt;

    @Column(name = "published_at", nullable = false)
    @Builder.Default
    private OffsetDateTime publishedAt = OffsetDateTime.now();

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "repliedToTweet", fetch = FetchType.LAZY)
    private List<Tweet> replies;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "retweetedTweet", fetch = FetchType.LAZY)
    private List<Tweet> retweets;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "quotedTweet", fetch = FetchType.LAZY)
    private List<Tweet> quoteTweets;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "tweet", fetch = FetchType.LAZY)
    private List<TweetHashtag> hashtags;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "tweet", fetch = FetchType.LAZY)
    private List<TweetMention> mentions;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "tweet", fetch = FetchType.LAZY)
    private List<Media> media;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "tweet", fetch = FetchType.LAZY)
    private List<Like> likes;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "tweet", fetch = FetchType.LAZY)
    private List<TweetEdit> editHistory;

    @OneToOne(mappedBy = "tweet", fetch = FetchType.LAZY)
    private Poll poll;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "tweet", fetch = FetchType.LAZY)
    private List<Notification> notifications;

    @Override
    public void redact()
    {
        this.content = "This post has been deleted.";
        this.scheduledAt = null;
        this.isPinned = false;
    }

    @Override
    public void onSoftDelete(EntityManager em)
    {
        hardDeleteWhere(em, Poll.class, "tweet", this);

        hardDeleteWhere(em, Media.class, "tweet", this);
        hardDeleteWhere(em, Like.class, "tweet", this);
        hardDeleteWhere(em, TweetHashtag.class, "tweet", this);
        hardDeleteWhere(em, TweetMention.class, "tweet", this);
        hardDeleteWhere(em, TweetEdit.class, "tweet", this);
        hardDeleteWhere(em, Notification.class, "tweet", this);
    }
}
