package logic_core.app.usecase.message;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.EditMessageRequest;
import logic_core.app.dto.response.ConversationInfoResponse;
import logic_core.app.dto.response.ConversationStateResponse;
import logic_core.app.dto.validator.MessageValidator;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.SessionUserContext;
import logic_core.common.exception.*;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.model.ConversationModel;
import logic_core.domain.model.MessageModel;
import logic_core.domain.policy.DirectMessagePolicy;
import logic_core.domain.repository.ConversationRepository;
import logic_core.domain.repository.DirectMessageRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EditMessageUseCase
{
    @NonNull private final MessageValidator validator;
    @NonNull private final DirectMessagePolicy policy;
    @NonNull private final DirectMessageRepository messageRepository;
    @NonNull private final ConversationRepository conversationRepository;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;
    @NonNull private final TimeProvider timeProvider;

    @Transactional
    public Result<ConversationStateResponse> execute(EditMessageRequest request)
    {
        if (request == null)
        {
            return Result.failure("Request cannot be null.");
        }

        try
        {
            SessionUserContext context =
                    lockOrchestrator.lockAndGetContextByToken(
                            request.sessionToken()
                    );

            UUID currentUserId = context.lockedUser().getId();

            validator.validateEditMessage(
                    currentUserId,
                    request.conversationId(),
                    request.messageId(),
                    request.text()
            );

            policy.validateEditMessage(
                    currentUserId,
                    request.conversationId(),
                    request.messageId()
            );

            MessageModel currentMessage = messageRepository.findByIdForUpdate(request.messageId())
                    .orElseThrow(() -> new NotFoundException("Message not found."));

            if (!request.conversationId().equals(currentMessage.getConversationId()))
            {
                return Result.failure("Message does not belong to this conversation.");
            }

            String newContent = request.text().trim();
            String previousContent = currentMessage.getContent();

            if (newContent.equals(previousContent))
            {
                return Result.success(buildResponse(request.conversationId(), currentUserId));
            }

            MessageModel editedMessage = MessageModel.builder()
                    .messageId(currentMessage.getMessageId())
                    .conversationId(currentMessage.getConversationId())
                    .senderId(currentMessage.getSenderId())
                    .content(newContent)
                    .isRead(currentMessage.isRead())
                    .sentAt(currentMessage.getSentAt())
                    .readAt(currentMessage.getReadAt())
                    .build();

            messageRepository.update(editedMessage);


            return Result.success(buildResponse(request.conversationId(), currentUserId));
        }
        catch (ValidationException | ForbiddenException | ConflictException | NotFoundException e)
        {
            return Result.failure(e.getMessage());
        }
        catch (Exception e)
        {
            return Result.failure("An unexpected error occurred during message editing.");
        }
    }

    private ConversationStateResponse buildResponse(UUID conversationId, UUID currentUserId)
    {
        ConversationModel conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found."));

        List<MessageModel> latestMessages = messageRepository.findLatestMessages(conversationId, 1);
        MessageModel lastMessage = latestMessages.isEmpty() ? null : latestMessages.get(0);

        long unreadCount = messageRepository.countUnreadMessages(conversationId, currentUserId);

        return new ConversationStateResponse(
                conversation.getConversationId(),
                lastMessage != null ? lastMessage.getContent() : null,
                lastMessage != null ? lastMessage.getSentAt() : null,
                (int) unreadCount,
                new ConversationInfoResponse(
                        conversation.getConversationId(),
                        conversation.getCreatedAt(),
                        conversation.getUpdatedAt(),
                        conversation.getParticipantIds()
                )
        );
    }
}
