package Shared.Models.User;

import Shared.Models.Follow.Follow;
import Shared.Models.HashtagFollow.HashtagFollow;
import Shared.Models.Like.Like;
import Shared.Models.MutableEntity;
import Shared.Models.Session.Session;
import Shared.Models.Tweet.Tweet;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends MutableEntity
{
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
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

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Session> sessions;

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<Tweet> tweets;

    @OneToMany(mappedBy = "following", fetch = FetchType.LAZY)
    private List<Follow> followers;

    @OneToMany(mappedBy = "follower", fetch = FetchType.LAZY)
    private List<Follow> following;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Like> likes;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<HashtagFollow> followedHashtags;
}
