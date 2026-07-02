package Shared.Models;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@MappedSuperclass
public abstract class BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)

    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
            return true;

        if (other == null || getClass() != other.getClass())
            return false;

        BaseEntity entity = (BaseEntity) other;
        return id != null && id.equals(entity.id);
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }
}
