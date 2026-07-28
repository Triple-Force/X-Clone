package logic_core.app.dto.validator;

import logic_core.common.exception.ValidationException;

import java.util.UUID;

public class MessageValidator
{
    private static final int MAX_MESSAGE_LENGTH = 1000;

    public void validateSendMessage(UUID senderUserId, UUID receiverUserId, String text)
    {
        requireNonNull(senderUserId, "message.sender.required");
        requireNonNull(receiverUserId, "message.receiver.required");
        validateText(text);
    }

    public void validateDeleteMessage(UUID actorId,
                                      UUID conversationId,
                                      UUID messageId)
    {
        requireNonNull(actorId, "message.actor.required");
        requireNonNull(conversationId, "message.conversation.required");
        requireNonNull(messageId, "message.id.required");
    }

    public void validateEditMessage(UUID actorId,
                                    UUID conversationId,
                                    UUID messageId,
                                    String newText)
    {
        requireNonNull(actorId, "message.actor.required");
        requireNonNull(conversationId, "message.conversation.required");
        requireNonNull(messageId, "message.id.required");

        validateText(newText);
    }

    public void validateGetMessage(UUID actorId,
                                   UUID conversationId,
                                   UUID messageId)
    {
        requireNonNull(actorId, "message.actor.required");
        requireNonNull(conversationId, "message.conversation.required");
        requireNonNull(messageId, "message.id.required");
    }

    public void validateGetConversationMessages(
            UUID actorId,
            UUID conversationId,
            int limit,
            int offset)
    {
        requireNonNull(actorId, "message.actor.required");
        requireNonNull(conversationId, "message.conversation.required");

        if (limit <= 0)
        {
            throw new ValidationException("message.limit.invalid");
        }

        if (limit > 100)
        {
            throw new ValidationException("message.limit.too.large");
        }

        if (offset < 0)
        {
            throw new ValidationException("message.offset.invalid");
        }
    }


    private void validateText(String text)
    {
        if (text == null || text.trim().isEmpty())
        {
            throw new ValidationException("message.text.required");
        }

        if (text.length() > MAX_MESSAGE_LENGTH)
        {
            throw new ValidationException("message.text.too.long");
        }
    }

    private void requireNonNull(Object value, String message)
    {
        if (value == null)
        {
            throw new ValidationException(message);
        }
    }
}