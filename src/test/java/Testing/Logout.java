package Testing;

import Client.Service.AuthClientService;
import logic_core.app.dto.response.AuthResponse;
import logic_core.app.dto.response.LogoutResponse;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class Logout extends XCloneTest2
{
    @Test
    void shouldLogoutSuccessfullyAndInvalidateSession() throws Exception
    {
        // Arrange

        AuthClientService auth =
                new AuthClientService(client);

        AuthClientService.AuthResult<AuthResponse> register =
                auth.register(
                        "Ali",
                        "ali@mail.com",
                        "Password123!",
                        "Ali"
                ).get(10, TimeUnit.SECONDS);

        assertTrue(register.isSuccess());

        String token = register.data().token();

        assertEquals(1, db.countSessions());

        // Act

        AuthClientService.AuthResult<LogoutResponse> logout =
                auth.logout() .get(10, TimeUnit.SECONDS);


        // Assert Response

        assertTrue(logout.isSuccess());

        assertNull(logout.errorCode());

        assertNull(logout.errorMessage());

        // Assert Database

        assertEquals(
                0,
                db.countSessions()
        );

        // Token must not be usable anymore

        AuthClientService.AuthResult<LogoutResponse> secondLogout =
                auth.logout().get(10, TimeUnit.SECONDS);

        assertFalse(secondLogout.isSuccess());

        assertNotNull(secondLogout.errorCode());

        assertNotNull(secondLogout.errorMessage());

        assertEquals(
                0,
                db.countSessions()
        );
    }
}
