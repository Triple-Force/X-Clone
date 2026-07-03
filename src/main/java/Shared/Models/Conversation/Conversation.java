package Shared.Models.Conversation;

import Shared.Models.ConversationMember.ConversationMember;
import Shared.Models.DirectMessage.DirectMessage;
import Shared.Models.MutableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

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
    @BatchSize(size = 20)
    @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY)
    private List<ConversationMember> members;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY)
    private List<DirectMessage> messages;

    @Override
    public void onSoftDelete(EntityManager em)
    {
        hardDeleteWhere(em, DirectMessage.class, "conversation", this);
        hardDeleteWhere(em, ConversationMember.class, "conversation", this);
    }
}
