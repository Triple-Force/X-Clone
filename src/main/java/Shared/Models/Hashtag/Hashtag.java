package Shared.Models.Hashtag;

import Shared.Models.ImmutableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hashtags")
public class Hashtag extends ImmutableEntity
{
    @Column(name = "tag", length = 100, nullable = false)
    private String tag;
}
