package Testing;

import Client.Service.AuthClientService;
import Client.Service.UserClientService;
import Shared.Models.User.User;
import jakarta.persistence.EntityManager;
import logic_core.app.dto.media.UploadFile;
import logic_core.app.dto.response.*;
import logic_core.common.result.Result;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class UserTest extends XCloneTest2
{

    private record TestUser(
            String username,
            String password,
            AuthResponse authResponse
    ) {}


    @Test
    void getProfile_success() throws Exception
    {
        AuthClientService auth =
                new AuthClientService(client);

        UserClientService user =
                new UserClientService(client);


        TestUser testUser =
                createAndLoginUser(auth);



        Result<ProfileInfoResponse> result =
                user.getProfile(
                        testUser.authResponse().userId()
                ).get();



        assertTrue(result.isSuccess());


        ProfileInfoResponse profile =
                result.getData();



        assertNotNull(profile);

        assertEquals(
                testUser.authResponse().userId(),
                profile.userId()
        );


        assertEquals(
                "Test User",
                profile.displayName()
        );


        assertEquals(0, profile.followers());
        assertEquals(0, profile.following());
        assertEquals(0, profile.tweets());
    }



    @Test
    void searchUsers_success() throws Exception
    {
        AuthClientService auth =
                new AuthClientService(client);

        UserClientService user =
                new UserClientService(client);



        createAndLoginUser(auth);



        auth.register(
                "ali",
                "ali@test.com",
                "Password123!",
                "Ali Ahmadi"
        ).get();



        auth.register(
                "amir",
                "amir@test.com",
                "Password123!",
                "Amir Hosseini"
        ).get();



        Result<List<UserSearchResponse>> result =
                user.searchUsers(
                        "ali",
                        10,
                        0
                ).get();



        assertTrue(result.isSuccess());


        List<UserSearchResponse> users =
                result.getData();



        assertEquals(
                1,
                users.size()
        );


        assertEquals(
                "ali",
                users.getFirst().username()
        );


        assertEquals(
                "Ali Ahmadi",
                users.getFirst().displayName()
        );
    }




    @Test
    void updateProfile_success() throws Exception
    {
        AuthClientService auth =
                new AuthClientService(client);

        UserClientService user =
                new UserClientService(client);



        TestUser testUser =
                createAndLoginUser(auth);


        Result<Void> result =
                user.updateProfile(
                        "new_username",
                        testUser.authResponse().userId(),
                        "New Display"
                ).get();



        assertTrue(result.isSuccess());



        EntityManager em =
                db.newEntityManager();



        User updated =
                em.find(
                        User.class,
                        testUser.authResponse().userId()
                );



        assertEquals(
                "new_username",
                updated.getUsername()
        );


        assertEquals(
                "New Display",
                updated.getDisplayName()
        );


        em.close();
    }




    @Test
    void updateBio_success() throws Exception
    {
        AuthClientService auth = new AuthClientService(client);

        UserClientService user = new UserClientService(client);

        TestUser testUser = createAndLoginUser(auth);

        Result<UpdateBioResponse> result =
                user.updateBio(
                        "My new bio"
                ).get();

        assertTrue(result.isSuccess());

        EntityManager em = db.newEntityManager();


        User updated = em.find(User.class, testUser.authResponse().userId());

        assertEquals("My new bio", updated.getBio());

        em.close();
    }


    @Test
    void updateEmail_success() throws Exception
    {
        AuthClientService auth = new AuthClientService(client);

        UserClientService user = new UserClientService(client);

        TestUser testUser = createAndLoginUser(auth);


        Result<Void> result = user.updateEmail(testUser.authResponse().userId(), "new@email.com").get();

        assertTrue(result.isSuccess());

        EntityManager em = db.newEntityManager();

        User updated = em.find(User.class, testUser.authResponse().userId());

        assertEquals("new@email.com", updated.getEmail()
        );


        em.close();
    }


    @Test
    void updatePassword_success() throws Exception
    {
        AuthClientService auth = new AuthClientService(client);

        UserClientService user = new UserClientService(client);

        TestUser testUser = createAndLoginUser(auth);

        Result<Void> result =
                user.updatePassword(
                        testUser.password(),
                        "NewPassword123!",
                        testUser.authResponse().userId()
                ).get();



        assertTrue(result.isSuccess());

        AuthClientService.AuthResult<AuthResponse> login =
                auth.login(
                        testUser.username(),
                        "NewPassword123!"
                ).get();

        assertTrue(login.isSuccess());
    }



    @Test
    void deleteAccount_success() throws Exception
    {
        AuthClientService auth = new AuthClientService(client);

        UserClientService user = new UserClientService(client);


        TestUser testUser = createAndLoginUser(auth);


        Result<Void> result = user.deleteAccount(testUser.password(), testUser.authResponse().userId()).get();

        assertTrue(result.isSuccess());

        assertFalse(db.userExists(testUser.username()));
    }


    @Test
    void updateAvatar_success() throws Exception
    {
        AuthClientService auth = new AuthClientService(client);

        UserClientService user = new UserClientService(client);

        createAndLoginUser(auth);

        UploadFile file = new UploadFile("avatar.png", "image/png", new byte[]{1,2,3,4});


        Result<UpdateAvatarResponse> result = user.updateAvatar(file).get();


        assertTrue(result.isSuccess());
    }



    @Test
    void updateBanner_success() throws Exception
    {
        AuthClientService auth = new AuthClientService(client);

        UserClientService user = new UserClientService(client);

        createAndLoginUser(auth);

        UploadFile file = new UploadFile("banner.png", "image/png", new byte[]{1,2,3,4});

        Result<UpdateBannerResponse> result = user.updateBanner(file).get();

        assertTrue(result.isSuccess());
    }


    private TestUser createAndLoginUser(
            AuthClientService auth
    ) throws Exception
    {
        String suffix =
                UUID.randomUUID()
                        .toString()
                        .substring(0,8);

        String username = "user_" + suffix;

        String email = username + "@test.com";


        String password = "Password123!";

        AuthClientService.AuthResult<AuthResponse> register = auth.register(username, email, password, "Test User").get();


        assertTrue(register.isSuccess());

        AuthClientService.AuthResult<AuthResponse> login = auth.login(username, password).get();

        assertTrue(login.isSuccess());

        return new TestUser(
                username,
                password,
                login.data()
        );
    }

    @Test
    void updateCompleteProfile_success() throws Exception
    {
        AuthClientService auth = new AuthClientService(client);

        UserClientService user = new UserClientService(client);

        TestUser testUser = createAndLoginUser(auth);

        UploadFile avatar = new UploadFile(
                "avatar.png",
                "image/png",
                new byte[]{1,2,3,4}
        );

        UploadFile banner = new UploadFile(
                "banner.png",
                "image/png",
                new byte[]{5,6,7,8}
        );

        Result<UpdateCompleteProfileResponse> result = user.updateCompleteProfile(
                testUser.authResponse().userId(),
                "New Display",
                "new_username",
                "My new bio",
                avatar,
                banner
        ).get();

        assertTrue(result.isSuccess());

        UpdateCompleteProfileResponse response = result.getData();

        assertNotNull(response.profile());

        EntityManager em =
                db.newEntityManager();

        User updated =
                em.find(
                        User.class,
                        testUser.authResponse().userId()
                );

        assertEquals("new_username", updated.getUsername());

        assertEquals("New Display", updated.getDisplayName());

        assertEquals("My new bio", updated.getBio());

        assertNotNull(updated.getAvatarUrl());

        assertNotNull(updated.getBannerUrl());

        em.close();
    }
}