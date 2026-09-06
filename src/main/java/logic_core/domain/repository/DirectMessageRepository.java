package logic_core.domain.repository;

import logic_core.domain.model.MessageModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DirectMessageRepository
{
    MessageModel save(MessageModel message);

    void update(MessageModel message);

    void softDelete(UUID messageId);

    Optional<MessageModel> findById(UUID id);

    Optional<MessageModel> findByIdForUpdate(UUID id);

    List<MessageModel> findByConversationId(UUID conversationId);

    List<MessageModel> findByConversationIdForUpdate(UUID conversationId);

    List<MessageModel> findLatestMessages(UUID conversationId, int limit);

    long countUnreadMessages(UUID conversationId, UUID receiverUserId);

    List<MessageModel> findByConversationId(UUID conversationId, int limit, int offset);

    Optional<MessageModel> findLastMessage(UUID conversationId);
}
