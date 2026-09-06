package logic_core.infrastructure.mapper;

import logic_core.domain.model.ConversationModel;
import logic_core.infrastructure.persistence.entity.conversation.ConversationEntity;
import logic_core.infrastructure.persistence.entity.conversationmember.ConversationMemberEntity;

import java.util.List;
import java.util.UUID;

public final class ConversationEntityMapper {

    private ConversationEntityMapper() {
    }

    /**
     * Maps a {@link ConversationEntity} plus its materialized membership rows to
     * the domain {@link ConversationModel}. The caller is responsible for loading
     * the members inside the active transaction (see
     * {@code ConversationMemberJpaRepository.findByConversationId(...)}).
     */
    public static ConversationModel toModel(
            ConversationEntity entity,
            List<ConversationMemberEntity> members) {
        if (entity == null) {
            return null;
        }

        List<UUID> participantIds = members == null
                ? List.of()
                : members.stream()
                        .map(m -> m.getUser() != null ? m.getUser().getId() : null)
                        .filter(java.util.Objects::nonNull)
                        .toList();

        return ConversationModel.builder()
                .conversationId(entity.getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .participantIds(participantIds)
                .build();
    }
}
