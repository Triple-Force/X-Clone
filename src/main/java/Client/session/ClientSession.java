package Client.session;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ClientSession
{
    private static final SessionSnapshot EMPTY = new SessionSnapshot(null, null);

    private final AtomicReference<SessionSnapshot> state =
            new AtomicReference<>(EMPTY);

    public void updateSession(String token, UUID userId)
    {
        state.set(new SessionSnapshot(token, userId));
    }

    public void clear()
    {
        state.set(EMPTY);
    }

    public String getToken()
    {
        return state.get().token();
    }

    public UUID getCurrentUserId()
    {
        return state.get().userId();
    }


    public boolean isLoggedIn()
    {
        return state.get().isLoggedIn();
    }


    public SessionSnapshot snapshot()
    {
        return state.get();
    }

    public record SessionSnapshot(String token, UUID userId)
    {

        public boolean isLoggedIn()
        {
            return token != null && !token.isBlank();
        }
    }
}