package logic_core.infrastructure.repository;

import logic_core.domain.model.SessionModel;
import logic_core.infrastructure.config.AppConfig;
import logic_core.infrastructure.persistence.entity.session.SessionEntity;
import logic_core.infrastructure.persistence.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import com.xclone.Application;
import org.springframework.test.context.ContextConfiguration;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = {
        Application.class,
        AppConfig.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(SessionRepositoryAdapter.class)
class SessionRepositoryAdapterIntegrationTest {
    @Autowired
    private SessionRepositoryAdapter sessionRepositoryAdapter;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hash");
        testUser = userJpaRepository.save(testUser);
    }

    @Test
    void createSession_shouldPersistSession() {
        String token = "test-token";
        OffsetDateTime expiresAt = OffsetDateTime.now().plusHours(1);

        SessionModel model = sessionRepositoryAdapter.createSession(testUser.getId(), token, expiresAt);

        assertThat(model.getId()).isNotNull();
        assertThat(model.getUserId()).isEqualTo(testUser.getId());
        assertThat(model.getToken()).isEqualTo(token);
        assertThat(model.getExpiresAt()).isEqualTo(expiresAt);

        SessionEntity persisted = testEntityManager.find(SessionEntity.class, model.getId());
        assertThat(persisted).isNotNull();
        assertThat(persisted.getToken()).isEqualTo(token);
        assertThat(persisted.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(persisted.getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void findById_shouldReturnSession() {
        SessionEntity entity = new SessionEntity();
        entity.setUser(testUser);
        entity.setToken("token");
        entity.setExpiresAt(OffsetDateTime.now().plusHours(1));
        entity = testEntityManager.persistFlushFind(entity);

        Optional<SessionModel> result = sessionRepositoryAdapter.findById(entity.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(entity.getId());
    }

    @Test
    void findByToken_shouldReturnSession() {
        String token = "unique-token";
        SessionEntity entity = new SessionEntity();
        entity.setUser(testUser);
        entity.setToken(token);
        entity.setExpiresAt(OffsetDateTime.now().plusHours(1));
        testEntityManager.persistAndFlush(entity);

        Optional<SessionModel> result = sessionRepositoryAdapter.findByToken(token);

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(token);
    }

    @Test
    void findActiveSessionsByUserId_shouldFilterCorrectly() {
        OffsetDateTime now = OffsetDateTime.now();
        
        // Active
        SessionEntity active = new SessionEntity();
        active.setUser(testUser);
        active.setToken("active");
        active.setExpiresAt(now.plusHours(1));
        testEntityManager.persist(active);
        
        // Expired
        SessionEntity expired = new SessionEntity();
        expired.setUser(testUser);
        expired.setToken("expired");
        expired.setExpiresAt(now.minusHours(1));
        testEntityManager.persist(expired);

        // Another user
        UserEntity otherUser = new UserEntity();
        otherUser.setUsername("other");
        otherUser.setEmail("other@example.com");
        otherUser.setPasswordHash("hash");
        otherUser = userJpaRepository.save(otherUser);
        SessionEntity otherSession = new SessionEntity();
        otherSession.setUser(otherUser);
        otherSession.setToken("other");
        otherSession.setExpiresAt(now.plusHours(1));
        testEntityManager.persist(otherSession);
        
        testEntityManager.flush();

        List<SessionModel> activeSessions = sessionRepositoryAdapter.findActiveSessionsByUserId(testUser.getId());

        assertThat(activeSessions).hasSize(1);
        assertThat(activeSessions.get(0).getToken()).isEqualTo("active");
    }

    @Test
    void revokeById_shouldRemoveSession() {
        SessionEntity entity = new SessionEntity();
        entity.setUser(testUser);
        entity.setToken("token");
        entity.setExpiresAt(OffsetDateTime.now().plusHours(1));
        entity = testEntityManager.persistFlushFind(entity);

        sessionRepositoryAdapter.revokeById(entity.getId());

        assertThat(testEntityManager.find(SessionEntity.class, entity.getId())).isNull();
    }

    @Test
    void revokeAllByUserId_shouldRemoveAllUserSessions() {
        SessionEntity s1 = new SessionEntity();
        s1.setUser(testUser);
        s1.setToken("s1");
        s1.setExpiresAt(OffsetDateTime.now().plusHours(1));
        testEntityManager.persist(s1);

        SessionEntity s2 = new SessionEntity();
        s2.setUser(testUser);
        s2.setToken("s2");
        s2.setExpiresAt(OffsetDateTime.now().plusHours(1));
        testEntityManager.persist(s2);
        
        testEntityManager.flush();

        sessionRepositoryAdapter.revokeAllByUserId(testUser.getId());

        assertThat(testEntityManager.getEntityManager().createQuery("select count(s) from SessionEntity s where s.user.id = :uid", Long.class)
                .setParameter("uid", testUser.getId())
                .getSingleResult()).isEqualTo(0L);
    }

    @Test
    void replaceUserSession_shouldReplaceOldWithNew() {
        SessionEntity old = new SessionEntity();
        old.setUser(testUser);
        old.setToken("old");
        old.setExpiresAt(OffsetDateTime.now().plusHours(1));
        old = testEntityManager.persistFlushFind(old);

        SessionModel newSession = SessionModel.builder()
                .userId(testUser.getId())
                .token("new")
                .expiresAt(OffsetDateTime.now().plusHours(2))
                .build();

        sessionRepositoryAdapter.replaceUserSession(testUser.getId(), newSession);
        
        testEntityManager.flush();

        assertThat(testEntityManager.find(SessionEntity.class, old.getId())).isNull();
        
        List<SessionEntity> userSessions = testEntityManager.getEntityManager()
                .createQuery("select s from SessionEntity s where s.user.id = :uid", SessionEntity.class)
                .setParameter("uid", testUser.getId())
                .getResultList();
                
        assertThat(userSessions).hasSize(1);
        assertThat(userSessions.get(0).getToken()).isEqualTo("new");
    }
}
