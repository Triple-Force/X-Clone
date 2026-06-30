package Shared.Models;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public class MutableEntity extends BaseEntity
{
    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
