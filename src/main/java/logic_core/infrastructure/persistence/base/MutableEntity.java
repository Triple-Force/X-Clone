package logic_core.infrastructure.persistence.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Getter
@MappedSuperclass
public abstract class MutableEntity extends BaseEntity {

    @UpdateTimestamp
    @Column(name="updated_at")
    private OffsetDateTime updatedAt;


    @Column(name="is_deleted", nullable=false)
    private boolean isDeleted=false;


    public void markDeleted(){
        this.isDeleted=true;
    }
}


