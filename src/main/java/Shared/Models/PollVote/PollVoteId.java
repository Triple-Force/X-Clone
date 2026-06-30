package Shared.Models.PollVote;

import Shared.Models.Poll.Poll;
import Shared.Models.User.User;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PollVoteId implements Serializable
{
    private User user;
    private Poll poll;
}
