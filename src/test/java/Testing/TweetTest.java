package Testing;

import Client.Service.AuthClientService;
import Client.Service.TweetClientService;
import logic_core.app.dto.response.AuthResponse;
import logic_core.app.dto.response.TweetResponse;
import logic_core.common.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TweetTest extends XCloneTest2
{
    private static final long TIMEOUT_SECONDS = 10;

    @Test
    @DisplayName("should create tweet successfully")
    void shouldCreateTweet() throws Exception
    {
        AuthClientService auth =
                new AuthClientService(client);

        TweetClientService tweetService =
                new TweetClientService(client);

        String username = "user_" + UUID.randomUUID().toString().substring(0, 8);

        String email = username + "@example.com";

        String password = "Password123!";

        //--------------------------------------------------
        // Register
        //--------------------------------------------------

        var register = auth.register(username, email, password, "Test User").get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(register.isSuccess(), register.errorMessage());

        //--------------------------------------------------
        // Create Tweet
        //--------------------------------------------------

        String content = "Hello XClone";

        Result<TweetResponse>  createResult = tweetService.createTweet(content, null, null, null, null).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(createResult.isSuccess(), createResult.getError());

        assertNotNull(createResult.getData());

        TweetResponse response = createResult.getData();

        //--------------------------------------------------
        // Validate Tweet
        //--------------------------------------------------

        TweetResponse tweet = createResult.getData();

        assertNotNull(tweet);

        assertNotNull(tweet.id());

        assertEquals(content, tweet.content());

        assertFalse(tweet.isDeleted());

        assertFalse(tweet.isPinned());

        assertNotNull(tweet.createdAt());

        assertNotNull(tweet.publishedAt());

        assertNull(tweet.repliedToId());

        assertNull(tweet.retweetedTweet());

        assertNull(tweet.quotedTweet());

        assertEquals(0L, tweet.likeCount());

        assertEquals(0L, tweet.replyCount());

        assertEquals(0L, tweet.retweetCount());

        assertNotNull(response.createdAt());
    }

    @Test
    @DisplayName("should edit tweet successfully")
    void shouldEditTweet() throws Exception
    {
        AuthClientService auth = new AuthClientService(client);

        TweetClientService tweetService = new TweetClientService(client);

        String username = "user_" + UUID.randomUUID().toString().substring(0, 8);

        String email = username + "@example.com";

        String password = "Password123!";

        //--------------------------------------------------
        // Register
        //--------------------------------------------------

        AuthClientService.AuthResult<AuthResponse> register = auth.register(username, email, password, "Test User").get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(register.isSuccess(), register.errorMessage());

        //--------------------------------------------------
        // Create Tweet
        //--------------------------------------------------

        String originalContent = "Original Tweet";

        Result<TweetResponse> createResult = tweetService.createTweet(originalContent, null, null, null, null).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(createResult.isSuccess(), createResult.getError());

        TweetResponse original = createResult.getData();

        //--------------------------------------------------
        // Edit Tweet
        //--------------------------------------------------

        String editedContent = "Edited Tweet Content";

        Result<TweetResponse> editResult = tweetService.editTweet(original.id(), editedContent).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(editResult.isSuccess(), editResult.getError());

        TweetResponse edited = editResult.getData();

        //--------------------------------------------------
        // Validate
        //--------------------------------------------------

        assertNotNull(edited);

        assertEquals(original.id(), edited.id());

        assertEquals(editedContent, edited.content());

        assertNotEquals(original.content(), edited.content());

        assertNotNull(edited.author());

        assertEquals(original.author().userId(), edited.author().userId());

        assertEquals(original.author().username(), edited.author().username());

        assertEquals(original.author().displayName(), edited.author().displayName());

        assertEquals(original.createdAt().toInstant(), edited.createdAt().toInstant());

        assertEquals(original.isDeleted(), edited.isDeleted());

        assertEquals(original.isPinned(), edited.isPinned());

        assertEquals(original.repliedToId(), edited.repliedToId());

        assertEquals(original.quotedTweet(), edited.quotedTweet());

        assertEquals(original.retweetedTweet(), edited.retweetedTweet());

        assertEquals(original.likeCount(), edited.likeCount());

        assertEquals(original.replyCount(), edited.replyCount());

        assertEquals(original.retweetCount(), edited.retweetCount());

        assertNotNull(edited.publishedAt());
    }


    @Test
    @DisplayName("should soft delete tweet successfully")
    void shouldDeleteTweet() throws Exception
    {
        AuthClientService auth = new AuthClientService(client);

        TweetClientService tweets = new TweetClientService(client);

        String username = "user_" + UUID.randomUUID().toString().substring(0, 8);

        String password = "Password123!";

        //--------------------------------------------------
        // Register
        //--------------------------------------------------

        AuthClientService.AuthResult<AuthResponse> register = auth.register(username, username + "@example.com", password, "Test User").get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(register.isSuccess());

        //--------------------------------------------------
        // Create Tweet
        //--------------------------------------------------

        Result<TweetResponse> created = tweets.createTweet("Tweet To Delete", null, null, null, null).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(created.isSuccess());

        UUID tweetId = created.getData().id();

        OffsetDateTime createdAt = created.getData().createdAt();

        //--------------------------------------------------
        // Delete Tweet
        //--------------------------------------------------

        Result<TweetResponse> deleted = tweets.deleteTweet(tweetId).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(deleted.isSuccess(), deleted.getError());

        TweetResponse tweet = deleted.getData();

        //--------------------------------------------------
        // Validate
        //--------------------------------------------------

        assertNotNull(tweet);

        assertEquals(tweetId, tweet.id());

        assertTrue(tweet.isDeleted());

        assertEquals("This post has been deleted.", tweet.content());

        assertEquals(createdAt.toInstant(), tweet.createdAt().toInstant());

        assertNotNull(tweet.publishedAt());

        assertEquals(username, tweet.author().username());

        assertEquals(register.data().userId(), tweet.author().userId());

        assertEquals(0L, tweet.likeCount());
        assertEquals(0L, tweet.replyCount());
        assertEquals(0L, tweet.retweetCount());

        assertNull(tweet.retweetedTweet());
        assertNull(tweet.quotedTweet());

        assertFalse(tweet.isPinned());
    }
}