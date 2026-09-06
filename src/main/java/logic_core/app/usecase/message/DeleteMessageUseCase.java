package logic_core.app.usecase.message;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.DeleteMessageRequest;
import logic_core.app.dto.response.ConversationInfoResponse;
import logic_core.app.dto.response.ConversationStateResponse;
import logic_core.app.dto.validator.MessageValidator;
import logic_core.app.security.AuthLockOrchestrator; // Import AuthLockOrchestrator
import logic_core.app.security.SessionUserContext; // Import SessionUserContext
// import logic_core.common.exception.ConflictException; // Consider migrating to AppException
// import logic_core.common.exception.ForbiddenException; // Consider migrating to AppException
// import logic_core.common.exception.NotFoundException; // Consider migrating to AppException
// import logic_core.common.exception.ValidationException; // Consider migrating to AppException
import logic_core.common.exception.AppException; // Use AppException for consistency
import logic_core.common.exception.ConflictException;
import logic_core.common.exception.ForbiddenException;
import logic_core.common.exception.NotFoundException;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.model.ConversationModel;
import logic_core.domain.model.MessageModel;
import logic_core.domain.repository.ConversationRepository;
import logic_core.domain.repository.DirectMessageRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteMessageUseCase
{
    @NonNull private final DirectMessageRepository directMessageRepository;
    @NonNull private final MessageValidator validator;
    @NonNull private final ConversationRepository conversationRepository;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<ConversationStateResponse> execute(DeleteMessageRequest request)
    {
        // Validation for request itself
        if (request == null || request.conversationId() == null || request.messageId() == null) {
            return Result.failure("Conversation ID and Message ID are required.");
        }

        try
        {
            SessionUserContext context =
                    lockOrchestrator.lockAndGetContextByToken(
                            request.sessionToken()
                    );

            UUID currentUserId = context.lockedUser().getId();
            UUID conversationId = request.conversationId();
            UUID messageId = request.messageId();

            // 2. Perform initial validations (could be moved to a dedicated validator if complex)
            validator.validateDeleteMessage(currentUserId, conversationId, messageId);

            // 3. Fetch Conversation and Message within the locked context
            ConversationModel conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new NotFoundException("Conversation not found.")); // Using NotFoundException

            if (!conversation.hasParticipant(currentUserId))
            {
                throw new ForbiddenException("You are not a participant of this conversation."); // Using ForbiddenException
            }

            // Use findByIdForUpdate to lock the message row for the duration of the transaction
            MessageModel message = directMessageRepository.findByIdForUpdate(messageId)
                    .orElseThrow(() -> new NotFoundException("Message not found.")); // Using NotFoundException

            if (!conversationId.equals(message.getConversationId()))
            {
                throw new ConflictException("Message does not belong to this conversation."); // Using ConflictException
            }

            if (!currentUserId.equals(message.getSenderId()))
            {
                throw new ForbiddenException("You are not allowed to delete this message."); // Using ForbiddenException
            }

            // 4. Perform the soft delete
            directMessageRepository.softDelete(message.getMessageId());

            // 6. Recalculate conversation state under lock
            // Fetch latest messages and unread count after deletion
            List<MessageModel> latestMessages =
                    directMessageRepository.findLatestMessages(conversationId, 1);
            MessageModel lastMessage = latestMessages.isEmpty() ? null : latestMessages.get(0);

            // Recalculate unread count for the current user in this conversation
            long unreadCount = directMessageRepository.countUnreadMessages(
                    conversation.getConversationId(),
                    currentUserId
            );

            // Construct the response
            ConversationStateResponse response = new ConversationStateResponse(
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

            return Result.success(response);
        }
        // Consolidated Exception Handling: Migrate to AppException for uniformity
        catch (AppException e) {
            return Result.failure(e.getMessage());
        }
        catch (Exception e) {
            // Catch-all for unexpected errors, potentially wrap in AppException
            // Consider logging the exception here for debugging
            // throw new AppException("An unexpected error occurred during message deletion.", e);
            return Result.failure("An unexpected error occurred during message deletion.");
        }
    }
}
