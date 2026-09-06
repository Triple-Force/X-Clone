package logic_core.infrastructure.repository;

import logic_core.domain.model.MessageModel;
import logic_core.domain.repository.DirectMessageRepository;
import logic_core.infrastructure.mapper.DirectMessageEntityMapper;
import logic_core.infrastructure.persistence.entity.directmessage.DirectMessageEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure adapter implementing {@link DirectMessageRepository} on top of
 * Spring Data JPA.
 * <p>
 * Replaces the legacy {@code JpaDirectMessageRepository} which delegated to
 * {@code DirectMessageDao} (a {@code GenericDAO} over the legacy
 * {@code Shared.Models.DirectMessage.DirectMessage} entity). Every method keeps
 * the exact behavior of the legacy DAO queries; the only legacy predicate that
 * is not reproduced is {@code conversation.isDeleted = false}, which is
 * unreachable because {@code Conversation.onSoftDelete()} hard-deletes all
 * messages of a conversation in the same transaction (see
 * {@code DirectMessageEntity}).
 */
@Component
@Transactional
public class DirectMessageRepositoryAdapter implements DirectMessageRepository {

    private static final String DELETED_CONTENT = "This message has been deleted.";

    private final DirectMessageJpaRepository directMessageJpaRepository;
    private final UserJpaRepository userJpaRepository;

    public DirectMessageRepositoryAdapter(DirectMessageJpaRepository directMessageJpaRepository,
                                          UserJpaRepository userJpaRepository) {
        this.directMessageJpaRepository = directMessageJpaRepository;
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public MessageModel save(MessageModel model) {
        Objects.requireNonNull(model, "messageModel must not be null.");

        DirectMessageEntity entity = DirectMessageEntityMapper.toPersistence(
                model,
                userJpaRepository.getReferenceById(model.getSenderId())
        );
        DirectMessageEntity saved = directMessageJpaRepository.save(entity);
        return DirectMessageEntityMapper.toDomain(saved);
    }

    @Override
    public void update(MessageModel model) {
        Objects.requireNonNull(model, "messageModel must not be null.");
        Objects.requireNonNull(model.getMessageId(), "messageId must not be null.");

        directMessageJpaRepository.findActiveById(model.getMessageId()).ifPresent(entity -> {
            DirectMessageEntityMapper.updateEntity(entity, model);
            directMessageJpaRepository.save(entity);
        });
    }

    @Override
    public void softDelete(UUID messageId) {
        if (messageId == null) {
            return;
        }

        directMessageJpaRepository.findActiveById(messageId).ifPresent(entity -> {
            entity.markDeleted();
            entity.setContent(DELETED_CONTENT);
            directMessageJpaRepository.save(entity);
        });
    }

    @Override
    public Optional<MessageModel> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }

        return directMessageJpaRepository.findActiveById(id)
                .map(DirectMessageEntityMapper::toDomain);
    }

    @Override
    public Optional<MessageModel> findByIdForUpdate(UUID id) {
        if (id == null) {
            return Optional.empty();
        }

        return directMessageJpaRepository.findActiveByIdForUpdate(id)
                .map(DirectMessageEntityMapper::toDomain);
    }

    @Override
    public List<MessageModel> findByConversationId(UUID conversationId) {
        if (conversationId == null) {
            return List.of();
        }

        return directMessageJpaRepository.findActiveByConversationId(conversationId)
                .stream()
                .map(DirectMessageEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<MessageModel> findByConversationIdForUpdate(UUID conversationId) {
        if (conversationId == null) {
            return List.of();
        }

        return directMessageJpaRepository.findActiveByConversationIdForUpdate(conversationId)
                .stream()
                .map(DirectMessageEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<MessageModel> findLatestMessages(UUID conversationId, int limit) {
        if (conversationId == null) {
            return List.of();
        }

        return directMessageJpaRepository.findLatestActiveByConversationId(
                        conversationId,
                        PageRequest.of(0, Math.max(1, limit))
                )
                .stream()
                .map(DirectMessageEntityMapper::toDomain)
                .toList();
    }

    @Override
    public long countUnreadMessages(UUID conversationId, UUID receiverUserId) {
        if (conversationId == null || receiverUserId == null) {
            return 0;
        }

        return directMessageJpaRepository.countUnreadMessages(conversationId, receiverUserId);
    }

    /**
     * Historical runtime semantics: the second argument is a 1-based page number
     * and the third is the page size (see {@code GetConversationMessagesUseCase},
     * which passes {@code page} and {@code pageSize}). This matches the old
     * {@code JpaDirectMessageRepository} implementation that computed
     * {@code offset = (page - 1) * pageSize} and passed the page size as the limit.
     */
    @Override
    public List<MessageModel> findByConversationId(UUID conversationId, int page, int pageSize) {
        if (conversationId == null) {
            return List.of();
        }

        return directMessageJpaRepository.findActiveByConversationId(
                        conversationId,
                        PageRequest.of(Math.max(0, page - 1), Math.max(1, pageSize))
                )
                .stream()
                .map(DirectMessageEntityMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<MessageModel> findLastMessage(UUID conversationId) {
        if (conversationId == null) {
            return Optional.empty();
        }

        return directMessageJpaRepository.findLatestActiveByConversationId(
                        conversationId,
                        PageRequest.of(0, 1)
                )
                .stream()
                .findFirst()
                .map(DirectMessageEntityMapper::toDomain);
    }
}
