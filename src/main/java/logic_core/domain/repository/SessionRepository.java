package logic_core.domain.repository;

import Shared.Models.Session.Session;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository
{
    public Optional<Session> findById(UUID sessionId);

    public Optional<Session> findByToken(String token);

    public List<Session> findActiveSessionsByUserId(UUID userId);

    Optional<Session> findByRefreshToken(String refreshToken);

    void save(Session session);

    void update(Session session);

    void revokeById(UUID sessionId);

    void revoke(Session session);

    Optional<OffsetDateTime> findExpireTimeById(UUID sessionId);
}
