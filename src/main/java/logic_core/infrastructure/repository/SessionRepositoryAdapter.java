package logic_core.infrastructure.repository;

import logic_core.domain.model.SessionModel;
import logic_core.domain.repository.SessionRepository;
import logic_core.infrastructure.mapper.SessionEntityMapper;
import logic_core.infrastructure.persistence.entity.session.SessionEntity;
import logic_core.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Transactional
public class SessionRepositoryAdapter implements SessionRepository {

    private final SessionJpaRepository sessionJpaRepository;
    private final UserJpaRepository userJpaRepository;

    public SessionRepositoryAdapter(SessionJpaRepository sessionJpaRepository, UserJpaRepository userJpaRepository) {
        this.sessionJpaRepository = sessionJpaRepository;
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public Optional<SessionModel> findById(UUID sessionId) {
        return sessionJpaRepository.findById(sessionId).map(SessionEntityMapper::toModel);
    }

    @Override
    public Optional<SessionModel> findByToken(String token) {
        return sessionJpaRepository.findByToken(token).map(SessionEntityMapper::toModel);
    }

    @Override
    public List<SessionModel> findActiveSessionsByUserId(UUID userId) {
        return sessionJpaRepository.findByUser_IdAndExpiresAtAfter(userId, OffsetDateTime.now())
                .stream().map(SessionEntityMapper::toModel).collect(Collectors.toList());
    }

    @Override
    public void revokeById(UUID sessionId) {
        sessionJpaRepository.deleteById(sessionId);
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        sessionJpaRepository.deleteAllByUserId(userId);
    }

    @Override
    public SessionModel createSession(UUID userId, String token, OffsetDateTime expiresAt) {
        UserEntity user = userJpaRepository.getReferenceById(userId);
        SessionEntity entity = SessionEntity.builder()
                .user(user)
                .token(token)
                .expiresAt(expiresAt)
                .build();
        return SessionEntityMapper.toModel(sessionJpaRepository.save(entity));
    }

    @Override
    public SessionModel replaceUserSession(UUID userId, SessionModel newSession)
    {

        sessionJpaRepository.deleteAllByUserId(userId);

        UserEntity user = userJpaRepository.getReferenceById(userId);

        SessionEntity entity = SessionEntity.builder()
                .user(user)
                .token(newSession.getToken())
                .expiresAt(newSession.getExpiresAt())
                .build();

        SessionEntity saved = sessionJpaRepository.save(entity);

        return SessionEntityMapper.toModel(saved);
    }
}
