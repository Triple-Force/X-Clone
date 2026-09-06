package logic_core.infrastructure.persistence.entity.conversationmember;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMemberEntityId implements Serializable {
    private UUID conversation;
    private UUID user;
}
