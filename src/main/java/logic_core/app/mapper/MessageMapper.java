package logic_core.app.mapper;

import logic_core.app.dto.response.MessageInfoResponse;
import logic_core.domain.model.MessageModel;

public final class MessageMapper
{
    private MessageMapper()
    {
    }

    public static MessageInfoResponse toResponse(MessageModel model)
    {
        return new MessageInfoResponse(
                model.getMessageId(),
                model.getConversationId(),
                model.getSenderId(),
                model.getContent(),
                model.getCreatedAt(),
                model.getUpdatedAt(),
                model.isRead(),
                model.isEdited()
        );
    }
}
