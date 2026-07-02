package Shared.Models.PollVote;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PollVoteId implements Serializable
{
    private UUID user;
    private UUID poll;
}
