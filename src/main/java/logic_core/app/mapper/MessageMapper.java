package logic_core.app.mapper;

import logic_core.app.dto.response.MessageResponse;
import logic_core.app.dto.response.UserResponse;
import logic_core.domain.model.MessageModel;

public final class MessageMapper
{
    private MessageMapper()
    {
    }

    public static MessageResponse toResponse(
            MessageModel message,
            UserResponse sender
    )
    {
        return new MessageResponse(
                message.getMessageId(),
                sender,
                message.getContent(),
                message.getSentAt()
        );
    }
}
