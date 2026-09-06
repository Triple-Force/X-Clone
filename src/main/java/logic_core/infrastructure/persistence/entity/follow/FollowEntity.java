package logic_core.infrastructure.persistence.entity.follow;

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
@Table(name = "follows", indexes = {
        @Index(name = "idx_follows_following_id", columnList = "following_id")
})
@Check(constraints = "follower_id <> following_id")
@IdClass(FollowEntityId.class)
public class FollowEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity follower;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "following_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity following;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
