package logic_core.domain.repository;

import logic_core.domain.model.SessionModel;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository {
    Optional<SessionModel> findById(UUID sessionId);
    Optional<SessionModel> findByToken(String token);
    List<SessionModel> findActiveSessionsByUserId(UUID userId);
    void revokeById(UUID sessionId);
    void revokeAllByUserId(UUID userId);
    SessionModel createSession(UUID userId, String token, OffsetDateTime expiresAt);
    SessionModel replaceUserSession(UUID userId, SessionModel newSession);

}
