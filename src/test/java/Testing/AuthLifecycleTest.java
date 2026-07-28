package Testing;

import Client.Service.AuthClientService;
import Client.Service.TweetClientService;
import logic_core.app.dto.response.AuthResponse;
import logic_core.app.dto.response.LikeResponse;
import logic_core.app.dto.response.TweetResponse;
import logic_core.common.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class AuthLifecycleTest extends XCloneTest2
{
    private static final long TIMEOUT_SECONDS = 10;
    private static final Pattern OTP_PATTERN = Pattern.compile(
            "\\[PASSWORD_RESET_OTP]\\s+email=(?<email>[^\\s]+)\\s+code=(?<code>\\d{6})"
    );

    @Test
    @DisplayName("should complete full auth lifecycle: register -> login -> reset password -> login again")
    void shouldCompleteFullAuthLifecycle() throws Exception
    {
        AuthClientService auth = new AuthClientService(client);

        String username = uniqueUsername();
        String email = uniqueEmail(username);
        String oldPassword = "OldPassword123!";
        String newPassword = "NewPassword123!";
        String displayName = "Lifecycle User";

        // 1) Register
        var registerResult = auth
                .register(username, email, oldPassword, displayName)
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(registerResult.isSuccess(), registerResult.errorMessage());
        assertNotNull(registerResult.data());
        assertEquals(username, registerResult.data().username());
        assertNotNull(registerResult.data().token());
        assertNotNull(registerResult.data().userId());

        // 2) Logout to simulate a fresh client state
        var logoutResult = auth
                .logout()
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(logoutResult.isSuccess(), logoutResult.errorMessage());
        assertFalse(client.session().isLoggedIn());

        // 3) Login with the original password
        rebuildClient();
        auth = new AuthClientService(client);

        var loginResult = auth
                .login(username, oldPassword)
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(loginResult.isSuccess(), loginResult.errorMessage());
        assertNotNull(loginResult.data());
        assertEquals(username, loginResult.data().username());
        assertNotNull(loginResult.data().token());

        // 4) Logout again before password reset
        var secondLogout = auth
                .logout()
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(secondLogout.isSuccess(), secondLogout.errorMessage());
        assertFalse(client.session().isLoggedIn());

        // 5) Request password reset and capture OTP from stdout
        String otp = requestPasswordResetAndCaptureOtp(auth, email);
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.chars().allMatch(Character::isDigit));

        // 6) Verify code
        var verifyResult = auth
                .verifyPasswordResetCode(email, otp)
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(verifyResult.isSuccess(), verifyResult.errorMessage());
        assertNotNull(verifyResult.data());

        // 7) Reset password
        var resetResult = auth
                .resetPassword(email, otp, newPassword)
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(resetResult.isSuccess(), resetResult.errorMessage());
        assertNotNull(resetResult.data());

        // 8) Rebuild client and login with new password
        rebuildClient();
        auth = new AuthClientService(client);

        var reloginResult = auth
                .login(username, newPassword)
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(reloginResult.isSuccess(), reloginResult.errorMessage());
        assertNotNull(reloginResult.data());
        assertEquals(username, reloginResult.data().username());
        assertNotNull(reloginResult.data().token());

        // 9) Final logout
        var finalLogout = auth
                .logout()
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(finalLogout.isSuccess(), finalLogout.errorMessage());
        assertFalse(client.session().isLoggedIn());
    }

    @Test
    @DisplayName("should reject invalid reset code and keep old password valid")
    void shouldRejectInvalidResetCodeAndKeepOldPassword() throws Exception
    {
        AuthClientService auth = new AuthClientService(client);

        String username = uniqueUsername();
        String email = uniqueEmail(username);
        String oldPassword = "OldPassword123!";
        String displayName = "Invalid Code User";

        var registerResult = auth
                .register(username, email, oldPassword, displayName)
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(registerResult.isSuccess(), registerResult.errorMessage());

        String otp = requestPasswordResetAndCaptureOtp(auth, email);
        String invalidOtp = corruptOtp(otp);

        var verifyResult = auth
                .verifyPasswordResetCode(email, invalidOtp)
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertFalse(verifyResult.isSuccess());
        assertNotNull(verifyResult.errorCode());
        assertNull(verifyResult.data());

        var resetResult = auth
                .resetPassword(email, invalidOtp, "NewPassword123!")
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertFalse(resetResult.isSuccess());
        assertNotNull(resetResult.errorCode());
        assertNull(resetResult.data());

        // Old password must still work
        rebuildClient();
        auth = new AuthClientService(client);

        var loginResult = auth
                .login(username, oldPassword)
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(loginResult.isSuccess(), loginResult.errorMessage());
        assertNotNull(loginResult.data());
        assertEquals(username, loginResult.data().username());

        assertNotNull(loginResult.data());

        assertEquals(
                username,
                loginResult.data().username()
        );

        assertEquals(
                registerResult.data().userId(),
                loginResult.data().userId()
        );

        assertNotNull(loginResult.data().token());

        assertTrue(client.session().isLoggedIn());

        var logout =
                auth.logout()
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(logout.isSuccess());

        assertFalse(client.session().isLoggedIn());
    }


    @Test
    @DisplayName("should like and unlike tweet successfully")
    void shouldLikeAndUnlikeTweet() throws Exception
    {
        AuthClientService auth =
                new AuthClientService(client);

        TweetClientService tweets =
                new TweetClientService(client);

        String username =
                "user_" + UUID.randomUUID().toString().substring(0, 8);

        String password =
                "Password123!";

        //--------------------------------------------------
        // Register
        //--------------------------------------------------

        AuthClientService.AuthResult<AuthResponse> register =
                auth.register(
                                username,
                                username + "@example.com",
                                password,
                                "Test User"
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(register.isSuccess());



        //--------------------------------------------------
        // Create Tweet
        //--------------------------------------------------

        Result<TweetResponse> created =
                tweets.createTweet(
                                "Like Test",
                                null,
                                null,
                                null,
                                null
                        )
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(created.isSuccess());

        UUID tweetId =
                created.getData().id();

        assertEquals(
                0,
                created.getData().likeCount()
        );

        //--------------------------------------------------
        // Like
        //--------------------------------------------------

        Result<LikeResponse> liked =
                tweets.likeTweet(tweetId)
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(liked.isSuccess());

        LikeResponse like =
                liked.getData();

        assertNotNull(like);

        assertEquals(tweetId, like.tweetId());

        assertTrue(like.liked());

        assertEquals(
                1,
                like.totalLikesCount()
        );

        //--------------------------------------------------
        // Unlike
        //--------------------------------------------------

        Result<LikeResponse> unliked =
                tweets.unlikeTweet(tweetId)
                        .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(unliked.isSuccess());

        LikeResponse unlike =
                unliked.getData();

        assertNotNull(unlike);

        assertEquals(tweetId, unlike.tweetId());

        assertFalse(unlike.liked());

        assertEquals(
                0,
                unlike.totalLikesCount()
        );
    }


    private String requestPasswordResetAndCaptureOtp(AuthClientService auth, String email) throws Exception
    {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try (PrintStream capture = new PrintStream(buffer, true, StandardCharsets.UTF_8))
        {
            System.setOut(capture);

            var result = auth
                    .requestPasswordReset(email)
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            assertTrue(result.isSuccess(), result.errorMessage());
        }
        finally
        {
            System.setOut(originalOut);
        }

        String output = buffer.toString(StandardCharsets.UTF_8);
        return extractOtpFromOutput(output, email);
    }

    private String extractOtpFromOutput(String output, String email)
    {
        Matcher matcher = OTP_PATTERN.matcher(output);

        while (matcher.find())
        {
            if (email.equals(matcher.group("email")))
            {
                return matcher.group("code");
            }
        }

        fail("Could not extract password reset OTP for email: " + email + "\nCaptured output:\n" + output);
        return null;
    }

    private static String corruptOtp(String otp)
    {
        if (otp == null || otp.length() != 6)
        {
            throw new IllegalArgumentException("OTP must be a 6-digit string.");
        }

        char last = otp.charAt(5);
        char replacement = last == '9' ? '0' : (char) (last + 1);
        return otp.substring(0, 5) + replacement;
    }

    private static String uniqueUsername()
    {
        return "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private static String uniqueEmail(String username)
    {
        return username + "@example.com";
    }
}
