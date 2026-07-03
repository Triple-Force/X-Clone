package Shared.Models;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Getter
@NoArgsConstructor
@Immutable
@MappedSuperclass
public class ImmutableEntity extends BaseEntity
{
}
