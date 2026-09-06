package logic_core.infrastructure.repository;

import logic_core.infrastructure.config.AppConfig;
import logic_core.infrastructure.persistence.entity.block.BlockEntity;
import logic_core.infrastructure.persistence.entity.like.LikeEntity;
import logic_core.infrastructure.persistence.entity.mute.MuteEntity;
import logic_core.infrastructure.persistence.entity.tweet.TweetEntity;
import logic_core.infrastructure.persistence.entity.UserEntity;
import logic_core.infrastructure.projection.TimelineTweetProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import com.xclone.Application;
import org.springframework.test.context.ContextConfiguration;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = {
        Application.class,
        AppConfig.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TweetJpaRepositoryLikedTimelineIntegrationTest {

    @Autowired
    private TweetJpaRepository tweetJpaRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private UserEntity actor;
    private UserEntity authorB;
    private UserEntity authorC;
    private UserEntity authorD;

    private TweetEntity b1;
    private TweetEntity b2;

    @BeforeEach
    void setUp() {
        OffsetDateTime base = OffsetDateTime.now().minusHours(1);

        actor = saveUser("actor");
        authorB = saveUser("authorB");
        authorC = saveUser("authorC");
        authorD = saveUser("authorD");

        // Tweets by B (muted author — must NOT be filtered out by LIKED)
        b1 = saveTweet(authorB, "B tweet 1", base.plusMinutes(3));
        b2 = saveTweet(authorB, "B tweet 2", base.plusMinutes(1));

        // Tweet by C (actor blocks C — must be filtered out)
        TweetEntity c1 = saveTweet(authorC, "C tweet", base.plusMinutes(2));

        // Tweet by D (D blocks actor — must be filtered out)
        TweetEntity d1 = saveTweet(authorD, "D tweet", base);

        // Soft-deleted tweet by B (must be filtered out)
        TweetEntity deleted = saveTweet(authorB, "deleted tweet", base.plusMinutes(4));
        deleted.markDeleted();
        testEntityManager.persistAndFlush(deleted);

        // Actor likes all five tweets
        like(actor, b1);
        like(actor, b2);
        like(actor, c1);
        like(actor, d1);
        like(actor, deleted);

        // Actor mutes B (LIKED has NO mute filter)
        MuteEntity mute = new MuteEntity();
        mute.setMuter(actor);
        mute.setMuted(authorB);
        testEntityManager.persistAndFlush(mute);

        // Actor blocks C (blocker = actor, blocked = author)
        BlockEntity blockByActor = new BlockEntity();
        blockByActor.setBlocker(actor);
        blockByActor.setBlocked(authorC);
        testEntityManager.persistAndFlush(blockByActor);

        // D blocks actor (blocker = author, blocked = actor)
        BlockEntity blockByAuthor = new BlockEntity();
        blockByAuthor.setBlocker(authorD);
        blockByAuthor.setBlocked(actor);
        testEntityManager.persistAndFlush(blockByAuthor);

        testEntityManager.flush();
        testEntityManager.clear();
    }

    @Test
    void findLikedTimeline_returnsOnlyActiveLikedTweetsWithBlockFilteringAndNoMuteFiltering() {
        List<TimelineTweetProjection> result =
                tweetJpaRepository.findLikedTimeline(
                        actor.getId(),
                        actor.getId(),
                        PageRequest.of(0, 20)
                );

        assertThat(result).hasSize(2);

        // ORDER BY publishedAt DESC
        assertThat(result.get(0).tweetId()).isEqualTo(b1.getId());
        assertThat(result.get(1).tweetId()).isEqualTo(b2.getId());

        // Muted author B's tweets ARE included (LIKED applies no mute filter)
        assertThat(result)
                .extracting(TimelineTweetProjection::tweetId)
                .containsExactly(b1.getId(), b2.getId());

        // Projection carries author info and like count
        assertThat(result.get(0).authorId()).isEqualTo(authorB.getId());
        assertThat(result.get(0).username()).isEqualTo("authorB");
        assertThat(result.get(0).likeCount()).isEqualTo(1L);
    }

    @Test
    void countLikedTimeline_matchesFilteredLikedTweets() {
        long count = tweetJpaRepository.countLikedTimeline(actor.getId(), actor.getId());

        assertThat(count).isEqualTo(2L);
    }

    @Test
    void findLikedTimeline_appliesPagination() {
        List<TimelineTweetProjection> firstPage =
                tweetJpaRepository.findLikedTimeline(
                        actor.getId(),
                        actor.getId(),
                        PageRequest.of(0, 1)
                );

        assertThat(firstPage).hasSize(1);
        assertThat(firstPage.get(0).tweetId()).isEqualTo(b1.getId());

        List<TimelineTweetProjection> secondPage =
                tweetJpaRepository.findLikedTimeline(
                        actor.getId(),
                        actor.getId(),
                        PageRequest.of(1, 1)
                );

        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).tweetId()).isEqualTo(b2.getId());
    }

    private UserEntity saveUser(String username) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("hash");
        return testEntityManager.persistFlushFind(user);
    }

    private TweetEntity saveTweet(UserEntity author, String content, OffsetDateTime publishedAt) {
        TweetEntity tweet = new TweetEntity();
        tweet.setAuthor(author);
        tweet.setContent(content);
        tweet.setPublishedAt(publishedAt);
        return testEntityManager.persistFlushFind(tweet);
    }

    private void like(UserEntity user, TweetEntity tweet) {
        LikeEntity like = new LikeEntity();
        like.setUser(user);
        like.setTweet(tweet);
        testEntityManager.persistAndFlush(like);
    }
}