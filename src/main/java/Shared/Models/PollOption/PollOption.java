package Shared.Models.PollOption;

import Shared.Models.ImmutableEntity;
import Shared.Models.Poll.Poll;
import Shared.Models.PollVote.PollVote;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "poll_options")
public class PollOption extends ImmutableEntity
{
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poll_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Poll poll;

    @Column(name = "option_text", nullable = false)
    private String optionText;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private short displayOrder = 0;

    @OneToMany(mappedBy = "option", fetch = FetchType.LAZY)
    private List<PollVote> votes;
}
