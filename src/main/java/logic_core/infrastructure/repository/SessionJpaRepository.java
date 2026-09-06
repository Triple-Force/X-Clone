package logic_core.infrastructure.repository;

import logic_core.infrastructure.persistence.entity.session.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionJpaRepository extends JpaRepository<SessionEntity, UUID> {
    Optional<SessionEntity> findByToken(String token);
    List<SessionEntity> findByUser_IdAndExpiresAtAfter(UUID userId, OffsetDateTime now);
    void deleteByToken(String token);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from SessionEntity s where s.user.id = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);


}
