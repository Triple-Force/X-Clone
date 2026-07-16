package logic_core.app.mapper;

import logic_core.app.dto.response.ConversationResponse;
import logic_core.app.dto.response.UserResponse;
import logic_core.domain.model.ConversationModel;

public final class ConversationMapper
{
    private ConversationMapper()
    {
    }

    public static ConversationResponse toResponse(
            ConversationModel conversation,
            UserResponse otherUser,
            String lastMessagePreview,
            int unreadCount
    )
    {
        return new ConversationResponse(
                conversation.getConversationId(),
                otherUser,
                lastMessagePreview,
                conversation.getUpdatedAt(),
                unreadCount
        );
    }
}
