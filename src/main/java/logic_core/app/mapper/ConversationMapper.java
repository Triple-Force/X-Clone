package logic_core.app.mapper;

import logic_core.app.dto.response.ConversationInfoResponse;
import logic_core.app.dto.response.ConversationStateResponse;
import logic_core.domain.model.ConversationModel;

public final class ConversationMapper
{
    private ConversationMapper()
    {
    }

    public static ConversationStateResponse toResponse(
            ConversationModel conversation,
            String lastMessagePreview,
            int unreadCount
    )
    {


        ConversationInfoResponse conversationInfoResponse = new ConversationInfoResponse(
                conversation.getConversationId(),
          conversation.getCreatedAt(),
          conversation.getUpdatedAt(),
          conversation.getParticipantIds()
        );


        return new ConversationStateResponse(
                conversation.getConversationId(),
                lastMessagePreview,
                conversation.getUpdatedAt(),
                unreadCount,
                conversationInfoResponse
        );
    }
}
