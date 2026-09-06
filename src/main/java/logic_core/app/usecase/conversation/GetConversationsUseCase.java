package logic_core.app.usecase.conversation;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.GetConversationsRequest;
import logic_core.app.dto.response.ConversationSummaryResponse;
import logic_core.app.dto.response.GetConversationsResponse;
import logic_core.common.util.TimeProvider;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.SessionUserContext;
import logic_core.common.result.Result;
import logic_core.domain.model.ConversationModel;
import logic_core.domain.model.MessageModel;
import logic_core.domain.model.UserModel;
import logic_core.domain.policy.ConversationPolicy;
import logic_core.domain.repository.ConversationRepository;
import logic_core.domain.repository.DirectMessageRepository;
import logic_core.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetConversationsUseCase
{
    @NonNull
    private final ConversationRepository conversationRepository;
    @NonNull private final DirectMessageRepository directMessageRepository;
    @NonNull private final UserRepository userRepository;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<GetConversationsResponse> execute(GetConversationsRequest request)
    {
        try
        {
            SessionUserContext context = lockOrchestrator.lockAndGetContextByToken(request.sessionToken());

            UUID currentUserId = context.lockedUser().getId();

            List<ConversationModel> conversations = conversationRepository.findConversationsByUserId(
                    currentUserId,
                            request.page(),
                            request.pageSize()
                    );

            long totalItems =
                    conversationRepository.countConversations(currentUserId);

            List<ConversationSummaryResponse> summaries = new ArrayList<>();

            for (ConversationModel conversation : conversations)
            {
                String title = "Conversation";

                for (UUID participantId : conversation.getParticipantIds())
                {
                    if (!participantId.equals(currentUserId))
                    {
                        UserModel user =
                                userRepository.findById(participantId)
                                        .orElse(null);

                        if (user != null)
                        {
                            title = user.getDisplayName();
                        }

                        break;
                    }
                }

                Optional<MessageModel> lastMessage =
                        directMessageRepository.findLastMessage(
                                conversation.getConversationId()
                        );

                String lastMessageText =
                        lastMessage.map(MessageModel::getContent)
                                .orElse("");

                var lastMessageTime =
                        lastMessage.map(MessageModel::getCreatedAt)
                                .orElse(conversation.getUpdatedAt());

                int unread =
                        (int) directMessageRepository.countUnreadMessages(
                                conversation.getConversationId(),
                                currentUserId
                        );

                summaries.add(
                        ConversationSummaryResponse.builder()
                                .conversationId(conversation.getConversationId())
                                .title(title)
                                .lastMessage(lastMessageText)
                                .lastMessageAt(lastMessageTime)
                                .unreadCount(unread)
                                .build()
                );
            }

            boolean hasNext = (long) request.page() * request.pageSize() < totalItems;


            return Result.success(
                    GetConversationsResponse.builder()
                            .conversations(summaries)
                            .totalItems(totalItems)
                            .page(request.page())
                            .pageSize(request.pageSize())
                            .hasNext(hasNext)
                            .build()
            );
        }
        catch (Exception e)
        {
            return Result.failure(e.getMessage());
        }
    }
}