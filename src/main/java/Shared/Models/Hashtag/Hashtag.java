package Shared.Models.Hashtag;

import Shared.Models.HashtagFollow.HashtagFollow;
import Shared.Models.ImmutableEntity;
import Shared.Models.TweetHashtag.TweetHashtag;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hashtags", indexes = {@Index(name = "idx_hashtags_tag", columnList = "tag", unique = true)})
public class Hashtag extends ImmutableEntity
{
    @Column(name = "tag", length = 100, nullable = false)
    private String tag;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "hashtag", fetch = FetchType.LAZY)
    private List<HashtagFollow> followers;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "hashtag", fetch = FetchType.LAZY)
    private List<TweetHashtag> taggedTweets;
}
