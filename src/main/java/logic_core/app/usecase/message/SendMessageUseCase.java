package logic_core.app.usecase.message;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.SendMessageRequest;
import logic_core.app.dto.response.ConversationInfoResponse;
import logic_core.app.dto.response.ConversationStateResponse;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.CurrentAuthContext;
import logic_core.app.security.SessionUserContext;
import logic_core.common.exception.*;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.message.MessageSentEvent;
import logic_core.domain.model.ConversationModel;
import logic_core.domain.model.MessageModel;
import logic_core.domain.repository.ConversationRepository;
import logic_core.domain.repository.DirectMessageRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class SendMessageUseCase
{
    @NonNull private final DirectMessageRepository directMessageRepository;
    @NonNull private final ConversationRepository conversationRepository;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final TimeProvider timeProvider;

    @Transactional
    public Result<ConversationStateResponse> execute(SendMessageRequest request)
    {
        if (request == null || request.conversationId() == null || request.text() == null || request.text().isBlank())
        {
            return Result.failure("Invalid request parameters.");
        }

        try
        {
            SessionUserContext context =
                    lockOrchestrator.lockAndGetContextByToken(
                            request.sessionToken()
                    );

            UUID currentUserId = context.lockedUser().getId();

            ConversationModel conversation = conversationRepository.findByIdForUpdate(request.conversationId())
                    .orElseThrow(() -> new NotFoundException("Conversation not found."));

            if (!conversation.hasParticipant(currentUserId))
            {
                return Result.failure("You are not a participant of this conversation.");
            }

            String content = request.text().trim();
            OffsetDateTime now = timeProvider.now();

            MessageModel message = MessageModel.builder()
                    .messageId(UUID.randomUUID())
                    .conversationId(conversation.getConversationId())
                    .senderId(currentUserId)
                    .content(content)
                    .isRead(false)
                    .sentAt(now)
                    .build();

            MessageModel savedMessage = directMessageRepository.save(message);

            eventPublisher.publish(new MessageSentEvent(
                    savedMessage.getMessageId(),
                    savedMessage.getConversationId(),
                    savedMessage.getSenderId(),
                    content,
                    now
            ));

            long unreadCount = directMessageRepository.countUnreadMessages(
                    conversation.getConversationId(),
                    currentUserId
            );

            return Result.success(new ConversationStateResponse(
                    conversation.getConversationId(),
                    savedMessage.getContent(),
                    savedMessage.getSentAt(),
                    (int) unreadCount,
                    new ConversationInfoResponse(
                            conversation.getConversationId(),
                            conversation.getCreatedAt(),
                            conversation.getUpdatedAt(),
                            conversation.getParticipantIds()
                    )
            ));
        }
        catch (Exception e)
        {
            return Result.failure(e.getMessage());
        }
    }
}
