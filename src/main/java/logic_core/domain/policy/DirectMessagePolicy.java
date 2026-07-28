package logic_core.domain.policy;

import logic_core.common.exception.ForbiddenException;
import logic_core.common.exception.NotFoundException;
import logic_core.domain.model.ConversationModel;
import logic_core.domain.model.MessageModel;
import logic_core.domain.model.UserModel;
import logic_core.domain.repository.ConversationRepository;
import logic_core.domain.repository.DirectMessageRepository;
import logic_core.domain.repository.RelationshipRepository;
import logic_core.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class DirectMessagePolicy
{
    @NonNull private final UserRepository userRepository;
    @NonNull private final RelationshipRepository relationshipRepository;
    @NonNull private final ConversationRepository conversationRepository;
    @NonNull private final DirectMessageRepository messageRepository;

    public void validateSend(UUID senderId, UUID receiverId)
    {
        requireNonNullId(senderId, "senderId");
        requireNonNullId(receiverId, "receiverId");

        if (Objects.equals(senderId, receiverId))
        {
            throw new ForbiddenException("You cannot send a direct message to yourself.");
        }

        validateBaseMessaging(senderId, receiverId);
    }

    public void validateDeleteMessage(UUID actorId,
                                      UUID conversationId,
                                      UUID messageId)
    {
        ConversationModel conversation =
                conversationRepository.findById(conversationId)
                        .orElseThrow(() ->
                                new NotFoundException("Conversation not found."));

        MessageModel message =
                messageRepository.findById(messageId)
                        .orElseThrow(() ->
                                new NotFoundException("Message not found."));

        if (!conversation.getParticipantIds().contains(actorId))
        {
            throw new ForbiddenException("Actor is not a participant.");
        }

        if (!message.getConversationId().equals(conversationId))
        {
            throw new ForbiddenException("Message does not belong to conversation.");
        }

        if (!message.getSenderId().equals(actorId))
        {
            throw new ForbiddenException("Only sender can delete message.");
        }
    }


    public void validateEditMessage(UUID actorId,
                                    UUID conversationId,
                                    UUID messageId)
    {
        ConversationModel conversation =
                conversationRepository.findById(conversationId)
                        .orElseThrow(() ->
                                new NotFoundException("Conversation not found."));

        MessageModel message =
                messageRepository.findById(messageId)
                        .orElseThrow(() ->
                                new NotFoundException("Message not found."));

        if (!conversation.getParticipantIds().contains(actorId))
        {
            throw new ForbiddenException("Actor is not a participant.");
        }

        if (!message.getConversationId().equals(conversationId))
        {
            throw new ForbiddenException("Message does not belong to conversation.");
        }

        if (!message.getSenderId().equals(actorId))
        {
            throw new ForbiddenException("Only sender can edit message.");
        }
    }

    public void validateGetMessage(UUID actorId,
                                   UUID conversationId,
                                   UUID messageId)
    {
        ConversationModel conversation =
                conversationRepository.findById(conversationId)
                        .orElseThrow(() ->
                                new NotFoundException("Conversation not found."));

        MessageModel message =
                messageRepository.findById(messageId)
                        .orElseThrow(() ->
                                new NotFoundException("Message not found."));

        if (!conversation.getParticipantIds().contains(actorId))
        {
            throw new ForbiddenException("Access denied.");
        }

        if (!message.getConversationId().equals(conversationId))
        {
            throw new ForbiddenException("Message does not belong to conversation.");
        }
    }

    public void validateGetConversationMessages(UUID actorId,
                                                UUID conversationId)
    {
        ConversationModel conversation =
                conversationRepository.findById(conversationId)
                        .orElseThrow(() ->
                                new NotFoundException("Conversation not found."));

        if (!conversation.getParticipantIds().contains(actorId))
        {
            throw new ForbiddenException("Access denied.");
        }
    }

    private void validateBaseMessaging(UUID senderId, UUID receiverId)
    {
        UserModel sender = userRepository.findById(senderId)
                .orElseThrow(() -> new NotFoundException("Sender not found."));

        UserModel receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new NotFoundException("Receiver not found."));

        if (!sender.isActive())
        {
            throw new ForbiddenException("Inactive users cannot send direct messages.");
        }

        if (!receiver.isActive())
        {
            throw new ForbiddenException("Cannot send direct messages to an inactive user.");
        }

        validateBlockBarrier(senderId, receiverId);
    }

    private void validateBlockBarrier(UUID senderId, UUID receiverId)
    {
        if (relationshipRepository.isBlockedBy(senderId, receiverId) ||
                relationshipRepository.isBlockedBy(receiverId, senderId))
        {
            throw new ForbiddenException(
                    "Direct message is not allowed because a block relation exists between users."
            );
        }
    }

    private void requireNonNullId(UUID id, String name)
    {
        Objects.requireNonNull(id, name + " must not be null");
    }
}
