package logic_core.domain.repository;

import Shared.Models.Conversation.Conversation;
import logic_core.domain.model.ConversationModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository
{
    Optional<ConversationModel> findById(UUID conversationId);

    List<ConversationModel> findConversationsByUserId(UUID userId);

    ConversationModel save(ConversationModel conversation);

    void update(ConversationModel model);

    Optional<ConversationModel> findDirectConversationBetween(UUID firstUserId, UUID secondUserId);

    void deleteById(UUID conversationId);

    boolean existsById(UUID conversationId);

    // Member operations
    void addMember(UUID conversationId, UUID memberId);

    void deleteMember(UUID conversationId, UUID memberId);

    Optional<ConversationModel> findByIdForUpdate(UUID id);
}
