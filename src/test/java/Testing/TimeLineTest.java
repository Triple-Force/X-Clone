package Testing;

import Client.Service.AuthClientService;
import Client.Service.TimelineClientService;
import Client.Service.TweetClientService;
import logic_core.app.dto.request.GetTimelineResponse;
import logic_core.app.dto.response.AuthResponse;
import logic_core.app.dto.response.TweetResponse;
import logic_core.domain.repository.TimelineType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class TimeLineTest  extends XCloneTest2
{
    private static final long TIMEOUT_SECONDS = 10;

    @Test
    void thisIsMyTimelineTest() throws Exception
    {
        //--------------------------------------------------
// Register User A
//--------------------------------------------------

        AuthClientService auth = new AuthClientService(client);

        String userAUsername =
                "userA_" + UUID.randomUUID().toString().substring(0, 8);

        AuthClientService.AuthResult<AuthResponse> userA =
                auth.register(
                                userAUsername,
                                userAUsername + "@example.com",
                                "Password123!",
                                "User A"
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(userA.isSuccess());

        UUID userAId = userA.data().userId();

        assertNotNull(userAId);

        //--------------------------------------------------
// Login User A
//--------------------------------------------------

        rebuildClient();

        auth = new AuthClientService(client);

        assertTrue(
                auth.login(
                                userAUsername,
                                "Password123!"
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .isSuccess()
        );

        TweetClientService userATweets =
                new TweetClientService(client);

        TimelineClientService userATimeline =
                new TimelineClientService(client);

        //--------------------------------------------------
// User A creates tweets
//--------------------------------------------------

        TweetResponse tweet1 =
                userATweets.createTweet(
                                "Timeline Test Tweet #1",
                                null,
                                null,
                                List.of(),
                                null
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .getData();

        assertNotNull(tweet1);
        assertNotNull(tweet1.id());

        Thread.sleep(30);

        TweetResponse tweet2 =
                userATweets.createTweet(
                                "Timeline Test Tweet #2",
                                null,
                                null,
                                List.of(),
                                null
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .getData();

        assertNotNull(tweet2);
        assertNotNull(tweet2.id());

        Thread.sleep(30);

        TweetResponse tweet3 =
                userATweets.createTweet(
                                "Timeline Test Tweet #3",
                                null,
                                null,
                                List.of(),
                                null
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .getData();

        assertNotNull(tweet3);
        assertNotNull(tweet3.id());

        UUID tweet1Id = tweet1.id();
        UUID tweet2Id = tweet2.id();
        UUID tweet3Id = tweet3.id();

        //--------------------------------------------------
        // USER Timeline
        //--------------------------------------------------

        var result =
                userATimeline.getTimeline(
                                TimelineType.USER,
                                userAId,   // actor
                                userAId,   // target
                                0,         // page
                                20         // pageSize
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        if (!result.isSuccess()) {
            System.out.println(result.getMessage());
        }

        assertTrue(result.isSuccess());
        GetTimelineResponse response = result.getData();

        assertNotNull(response);

        assertEquals(3, response.tweets().size());

        assertEquals(3, response.totalItems());

        assertEquals(0, response.page());

        assertEquals(20, response.pageSize());

        assertFalse(response.hasNext());

        // newest first

        assertEquals(tweet3Id, response.tweets().get(0).tweetId());
        assertEquals(tweet2Id, response.tweets().get(1).tweetId());
        assertEquals(tweet1Id, response.tweets().get(2).tweetId());

        assertEquals(
                "Timeline Test Tweet #3",
                response.tweets().get(0).content()
        );

        assertEquals(
                "Timeline Test Tweet #2",
                response.tweets().get(1).content()
        );

        assertEquals(
                "Timeline Test Tweet #1",
                response.tweets().get(2).content()
        );


        //--------------------------------------------------
        // USER Timeline - Pagination
        //--------------------------------------------------

        result =
                userATimeline.getTimeline(
                                TimelineType.USER,
                                userAId,
                                userAId,
                                0,
                                2
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(result.isSuccess());

        response = result.getData();

        assertEquals(2, response.tweets().size());

        assertEquals(tweet3Id, response.tweets().get(0).tweetId());
        assertEquals(tweet2Id, response.tweets().get(1).tweetId());

        assertTrue(response.hasNext());

        //--------------------------------------------------

        result =
                userATimeline.getTimeline(
                                TimelineType.USER,
                                userAId,
                                userAId,
                                1,
                                2
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(result.isSuccess());

        response = result.getData();

        assertEquals(1, response.tweets().size());

        assertEquals(tweet1Id, response.tweets().get(0).tweetId());

        assertFalse(response.hasNext());


        //--------------------------------------------------
        // User A replies to Tweet #1
        //--------------------------------------------------

        Thread.sleep(30);

        TweetResponse reply =
                userATweets.replyTweet(
                                tweet1Id,
                                "Reply To Tweet #1",
                                List.of()
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .getData();

        assertNotNull(reply);
        assertNotNull(reply.id());

        UUID replyId = reply.id();

        //--------------------------------------------------
        // REPLIES Timeline
        //--------------------------------------------------

        result =
                userATimeline.getTimeline(
                                TimelineType.REPLIES,
                                userAId,
                                userAId,
                                0,
                                20
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(result.isSuccess());

        response = result.getData();

        assertNotNull(response);

        assertEquals(1, response.tweets().size());

        assertEquals(1, response.totalItems());

        assertEquals(replyId, response.tweets().get(0).tweetId());

        assertEquals(
                "Reply To Tweet #1",
                response.tweets().get(0).content()
        );

        assertFalse(response.hasNext());

        //--------------------------------------------------
        // Like Tweet #2
        //--------------------------------------------------

        var likeResult =
                userATweets.likeTweet(tweet2Id)
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(likeResult.isSuccess());

        //--------------------------------------------------
        // LIKED Timeline
        //--------------------------------------------------

        result =
                userATimeline.getTimeline(
                                TimelineType.LIKED,
                                userAId,
                                userAId,
                                0,
                                20
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(result.isSuccess());

        response = result.getData();

        assertNotNull(response);

        assertEquals(1, response.tweets().size());

        assertEquals(1, response.totalItems());

        assertEquals(
                tweet2Id,
                response.tweets().get(0).tweetId()
        );

        assertEquals(
                "Timeline Test Tweet #2",
                response.tweets().get(0).content()
        );

        assertFalse(response.hasNext());


        //--------------------------------------------------
        // MEDIA Timeline (currently empty)
        //--------------------------------------------------

        result =
                userATimeline.getTimeline(
                                TimelineType.MEDIA,
                                userAId,
                                userAId,
                                0,
                                20
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(result.isSuccess());

        response = result.getData();

        assertNotNull(response);

        assertEquals(0, response.tweets().size());

        assertEquals(0, response.totalItems());

        assertFalse(response.hasNext());


        //--------------------------------------------------
        // Register User B
        //--------------------------------------------------

        rebuildClient();

        auth = new AuthClientService(client);

        String userBUsername =
                "userB_" + UUID.randomUUID().toString().substring(0, 8);

        AuthClientService.AuthResult<AuthResponse> userB =
                auth.register(
                        userBUsername,
                        userBUsername + "@example.com",
                        "Password123!",
                        "User B"
                ).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(userB.isSuccess());

        UUID userBId = userB.data().userId();

        //--------------------------------------------------
        // Login User B
        //--------------------------------------------------

        rebuildClient();

        auth = new AuthClientService(client);

        assertTrue(
                auth.login(
                                userBUsername,
                                "Password123!"
                        ).get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .isSuccess()
        );

        TweetClientService userBTweets =
                new TweetClientService(client);

        TimelineClientService userBTimeline =
                new TimelineClientService(client);

        //--------------------------------------------------
        // User B creates one tweet
        //--------------------------------------------------

        TweetResponse userBTweet =
                userBTweets.createTweet(
                                "Tweet From User B",
                                null,
                                null,
                                List.of(),
                                null
                        ).get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .getData();

        assertNotNull(userBTweet);

        UUID userBTweetId = userBTweet.id();


        //--------------------------------------------------
        // Login User A Again
        //--------------------------------------------------

        rebuildClient();

        auth = new AuthClientService(client);

        assertTrue(
                auth.login(
                                userAUsername,
                                "Password123!"
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .isSuccess()
        );
// TODO: Used in HOME timeline tests after Follow use case is implemented.

        userATweets = new TweetClientService(client);

        userATimeline = new TimelineClientService(client);

    }
}
