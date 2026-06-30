package Shared.Models.Conversation;

import Shared.Models.ConversationMember.ConversationMember;
import Shared.Models.DirectMessage.DirectMessage;
import Shared.Models.MutableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "conversations")
public class Conversation extends MutableEntity
{
    @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY)
    private List<ConversationMember> members;

    @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY)
    private List<DirectMessage> messages;
}
