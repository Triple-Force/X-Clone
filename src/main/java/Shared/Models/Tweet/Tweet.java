package Shared.Models.Tweet;

import Shared.Models.Like.Like;
import Shared.Models.Media.Media;
import Shared.Models.MutableEntity;
import Shared.Models.TweetEdit.TweetEdit;
import Shared.Models.TweetHashtag.TweetHashtag;
import Shared.Models.TweetMention.TweetMention;
import Shared.Models.User.User;
import jakarta.persistence.*;
import lombok.*;
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
@Table(name = "tweets")
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

    @OneToMany(mappedBy = "repliedToTweet", fetch = FetchType.LAZY)
    private List<Tweet> replies;

    @OneToMany(mappedBy = "retweetedTweet", fetch = FetchType.LAZY)
    private List<Tweet> retweets;

    @OneToMany(mappedBy = "quotedTweet", fetch = FetchType.LAZY)
    private List<Tweet> quoteTweets;

    @OneToMany(mappedBy = "tweet", fetch = FetchType.LAZY)
    private List<TweetHashtag> hashtags;

    @OneToMany(mappedBy = "tweet", fetch = FetchType.LAZY)
    private List<TweetMention> mentions;

    @OneToMany(mappedBy = "tweet", fetch = FetchType.LAZY)
    private List<Media> media;

    @OneToMany(mappedBy = "tweet", fetch = FetchType.LAZY)
    private List<Like> likes;

    @OneToMany(mappedBy = "tweet", fetch = FetchType.LAZY)
    private List<TweetEdit> editHistory;
}
