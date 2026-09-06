package logic_core.infrastructure.persistence.entity.mute;

import jakarta.persistence.*;
import logic_core.infrastructure.persistence.entity.UserEntity;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mutes", indexes = {
    @Index(name = "idx_mutes_muted_id", columnList = "muted_id")
})
@Check(constraints = "muter_id <> muted_id")
@IdClass(MuteEntityId.class)
public class MuteEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "muter_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity muter;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "muted_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity muted;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
