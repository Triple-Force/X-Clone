package logic_core.infrastructure.repository;

import logic_core.domain.model.ConversationModel;
import logic_core.domain.repository.ConversationRepository;
import logic_core.infrastructure.mapper.ConversationEntityMapper;
import logic_core.infrastructure.persistence.entity.conversation.ConversationEntity;
import logic_core.infrastructure.persistence.entity.conversationmember.ConversationMemberEntity;
import logic_core.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure adapter implementing {@link ConversationRepository} on top of
 * Spring Data JPA.
 * <p>
 * Replaces the legacy {@code JpaConversationRepository} which delegated to
 * {@code ConversationDao} / {@code ConversationMemberDao} (GenericDAOs over the
 * legacy {@code Shared.Models.Conversation.*} entities). Behavior mirrors the
 * legacy queries exactly; members are materialized explicitly inside the
 * transaction instead of via lazy collection loading, so every returned
 * {@link ConversationModel} carries correct {@code participantIds}.
 */
@Component
@Transactional
public class ConversationRepositoryAdapter implements ConversationRepository {

    private final ConversationJpaRepository conversationJpaRepository;
    private final ConversationMemberJpaRepository conversationMemberJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final DirectMessageJpaRepository directMessageJpaRepository;

    public ConversationRepositoryAdapter(ConversationJpaRepository conversationJpaRepository,
                                         ConversationMemberJpaRepository conversationMemberJpaRepository,
                                         UserJpaRepository userJpaRepository,
                                         DirectMessageJpaRepository directMessageJpaRepository) {
        this.conversationJpaRepository = conversationJpaRepository;
        this.conversationMemberJpaRepository = conversationMemberJpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.directMessageJpaRepository = directMessageJpaRepository;
    }

    @Override
    public Optional<ConversationModel> findById(UUID conversationId) {
        if (conversationId == null) {
            return Optional.empty();
        }

        return conversationJpaRepository.findActiveById(conversationId)
                .map(this::toModel);
    }

    @Override
    public Optional<ConversationModel> findByIdForUpdate(UUID id) {
        if (id == null) {
            return Optional.empty();
        }

        return conversationJpaRepository.findActiveByIdForUpdate(id)
                .map(this::toModel);
    }

    @Override
    public List<ConversationModel> findConversationsByUserId(UUID userId, int page, int pageSize) {
        if (userId == null) {
            return List.of();
        }

        return conversationJpaRepository.findConversationsByUserId(
                        userId,
                        PageRequest.of(Math.max(0, page - 1), Math.max(1, pageSize))
                )
                .stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public long countConversations(UUID userId) {
        if (userId == null) {
            return 0;
        }

        return conversationJpaRepository.countConversations(userId);
    }

    @Override
    public ConversationModel save(ConversationModel model) {
        Objects.requireNonNull(model, "conversationModel must not be null.");

        ConversationEntity entity = conversationJpaRepository.save(new ConversationEntity());

        List<UUID> participants = model.getParticipantIds() == null
                ? List.of()
                : List.copyOf(model.getParticipantIds());

        for (UUID participantId : participants) {
            ConversationMemberEntity member = new ConversationMemberEntity();
            member.setConversation(entity);
            member.setUser(userJpaRepository.getReferenceById(participantId));
            conversationMemberJpaRepository.save(member);
        }

        // Flush so the generated timestamps are available on the returned model.
        conversationJpaRepository.flush();

        return ConversationModel.builder()
                .conversationId(entity.getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .participantIds(participants)
                .build();
    }

    /**
     * Preserves the legacy behavior exactly: the conversation table has no
     * mutable business columns and the legacy member re-sync ran against an
     * un-fetched member list, so {@code update(...)} performed no observable
     * write. Kept as a guarded no-op.
     */
    @Override
    public void update(ConversationModel model) {
        if (model == null || model.getConversationId() == null) {
            return;
        }

        conversationJpaRepository.findActiveById(model.getConversationId());
    }

    @Override
    public Optional<ConversationModel> findDirectConversationBetween(UUID firstUserId, UUID secondUserId) {
        if (firstUserId == null || secondUserId == null) {
            return Optional.empty();
        }

        return conversationJpaRepository.findDirectConversationBetween(firstUserId, secondUserId)
                .map(this::toModel);
    }

    /**
     * Soft-deletes the conversation and, in the same transaction, hard-deletes its
     * member rows and its direct messages — reproducing the legacy
     * {@code Conversation.onSoftDelete()} cascade.
     */
    @Override
    public void deleteById(UUID conversationId) {
        if (conversationId == null) {
            return;
        }

        conversationJpaRepository.findActiveById(conversationId).ifPresent(entity -> {
            entity.markDeleted();
            conversationJpaRepository.save(entity);
            conversationMemberJpaRepository.deleteByConversationId(conversationId);
            directMessageJpaRepository.hardDeleteByConversationId(conversationId);
        });
    }

    @Override
    public boolean existsById(UUID conversationId) {
        if (conversationId == null) {
            return false;
        }

        return conversationJpaRepository.existsByIdAndIsDeletedFalse(conversationId);
    }

    @Override
    public void addMember(UUID conversationId, UUID memberId) {
        if (conversationId == null || memberId == null) {
            return;
        }

        ConversationEntity conversation = conversationJpaRepository.getReferenceById(conversationId);
        UserEntity user = userJpaRepository.getReferenceById(memberId);

        ConversationMemberEntity member = new ConversationMemberEntity();
        member.setConversation(conversation);
        member.setUser(user);
        conversationMemberJpaRepository.save(member);
    }

    @Override
    public void deleteMember(UUID conversationId, UUID memberId) {
        ConversationMemberEntity relation = conversationMemberJpaRepository.findActiveRelation(conversationId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation member relation not found."));

        conversationMemberJpaRepository.delete(relation);
    }

    private ConversationModel toModel(ConversationEntity entity) {
        List<ConversationMemberEntity> members =
                conversationMemberJpaRepository.findByConversationId(entity.getId());

        return ConversationEntityMapper.toModel(entity, members);
    }
}
