package Shared.Models.Mute;

import Shared.Models.User.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mutes", check = @CheckConstraint(name = "no_self_mute", constraint = "muter_id <> muted_id"),
        indexes = {@Index(name = "idx_mutes_muted_id", columnList = "muted_id")})
@IdClass(MuteId.class)
public class Mute
{
    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "muter_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User muter;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "muted_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User muted;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
