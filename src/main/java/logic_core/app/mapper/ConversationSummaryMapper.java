package logic_core.app.mapper;

import logic_core.app.dto.response.ConversationSummaryResponse;
import logic_core.domain.model.ConversationModel;
import logic_core.domain.model.MessageModel;

import java.util.UUID;

public final class ConversationSummaryMapper {

    private ConversationSummaryMapper() {
    }

    public static ConversationSummaryResponse toResponse(
            ConversationModel conversation,
            MessageModel lastMessage,
            int unreadCount,
            UUID currentUserId
    ) {

        String title =
                conversation.getParticipantIds()
                        .stream()
                        .filter(id -> !id.equals(currentUserId))
                        .findFirst()
                        .map(UUID::toString)
                        .orElse("Conversation");

        return ConversationSummaryResponse.builder()
                .conversationId(conversation.getConversationId())
                .title(title)
                .lastMessage(lastMessage == null ? "" : lastMessage.getContent())
                .lastMessageAt(
                        lastMessage == null
                                ? conversation.getUpdatedAt()
                                : lastMessage.getCreatedAt()
                )
                .unreadCount(unreadCount)
                .build();
    }
}