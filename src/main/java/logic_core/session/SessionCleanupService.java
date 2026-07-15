package logic_core.session;

import Shared.Models.Session.Session;
import logic_core.infrastructure.dao.SessionDao;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@RequiredArgsConstructor
public class SessionCleanupService
{
    private final SessionDao sessionDao;

    public int cleanupExpiredSessions()
    {
        List<Session> allSessions = sessionDao.findAll();
        int count = 0;

        for (Session session : allSessions)
        {
            if (session.getExpiresAt().isBefore(OffsetDateTime.now()))
            {
                sessionDao.delete(session);
                count++;
             }
        }
        return count;
    }
}
