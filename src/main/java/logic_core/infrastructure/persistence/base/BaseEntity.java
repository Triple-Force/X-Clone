package logic_core.infrastructure.persistence.base;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;

        if (other == null)
            return false;

        if (org.hibernate.Hibernate.getClass(this) !=
                org.hibernate.Hibernate.getClass(other))
            return false;

        BaseEntity entity = (BaseEntity) other;

        return id != null && id.equals(entity.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
