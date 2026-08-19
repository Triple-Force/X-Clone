package Shared.Models.ConversationMember;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMemberId implements Serializable
{
    private UUID conversation;
    private UUID user;
}
