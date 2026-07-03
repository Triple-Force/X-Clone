package Shared.Models.Poll;

import Shared.Models.ImmutableEntity;
import Shared.Models.PollOption.PollOption;
import Shared.Models.Tweet.Tweet;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "polls")
public class Poll extends ImmutableEntity
{
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tweet_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tweet tweet;

    @Column(name = "question", nullable = false)
    private String question;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @OneToMany(mappedBy = "poll", fetch = FetchType.LAZY)
    private List<PollOption> options;
}
