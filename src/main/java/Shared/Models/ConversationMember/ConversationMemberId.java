package Shared.Models.ConversationMember;

import Shared.Models.Conversation.Conversation;
import Shared.Models.User.User;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMemberId implements Serializable
{
    private Conversation conversation;
    private User user;
}
