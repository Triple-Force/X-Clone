package logic_core.infrastructure.mapper;

import logic_core.domain.model.MessageModel;
import logic_core.infrastructure.persistence.entity.directmessage.DirectMessageEntity;
import logic_core.infrastructure.persistence.entity.UserEntity;

import java.time.ZoneOffset;
import java.util.Objects;

public final class DirectMessageEntityMapper {

    private DirectMessageEntityMapper() {
    }

    /**
     * Maps a {@link DirectMessageEntity} to the domain {@link MessageModel}.
     * <p>
     * Field-for-field equivalent of the legacy
     * {@code DirectMessagePersistenceMapper.toDomain(...)}: {@code readAt} and
     * {@code updatedAt} are intentionally not populated so that
     * {@link MessageModel#isRead()} and the {@code updatedAt} field of the API
     * responses keep exactly the same values they had under the legacy mapper.
     */
    public static MessageModel toDomain(DirectMessageEntity entity) {
        if (entity == null) {
            return null;
        }

        return MessageModel.builder()
                .messageId(entity.getId())
                .conversationId(entity.getConversationId())
                .senderId(entity.getSender() != null ? entity.getSender().getId() : null)
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .isRead(entity.isRead())
                .edited(entity.isEdited())
                .sentAt(entity.getCreatedAt() != null
                        ? entity.getCreatedAt().toInstant().atOffset(ZoneOffset.UTC)
                        : null)
                .build();
    }

    /**
     * Creates a new {@link DirectMessageEntity} from a {@link MessageModel}.
     * <p>
     * The caller must provide a resolved {@link UserEntity} sender reference
     * (use {@code UserJpaRepository.getReferenceById(...)}). Mirrors the legacy
     * create path: the ID and timestamps are database-generated, the model's
     * pre-set message ID is ignored, and {@code isEdited} always starts false.
     */
    public static DirectMessageEntity toPersistence(MessageModel model, UserEntity sender) {
        Objects.requireNonNull(model, "model must not be null");
        Objects.requireNonNull(sender, "sender must not be null");
        Objects.requireNonNull(model.getConversationId(), "conversationId must not be null");
        Objects.requireNonNull(model.getContent(), "content must not be null");

        DirectMessageEntity entity = new DirectMessageEntity();
        entity.setConversationId(model.getConversationId());
        entity.setSender(sender);
        entity.setContent(model.getContent());
        entity.setRead(model.isRead());
        entity.setEdited(false);
        return entity;
    }

    /**
     * Copies mutable fields of a {@link MessageModel} onto an existing entity.
     * <p>
     * Equivalent to the legacy {@code DirectMessagePersistenceMapper.updateEntity(...)}:
     * content is replaced when non-null, and the read/edited flags are copied from
     * the model (the edited flag is therefore never set true by the current edit flow,
     * exactly as under the legacy implementation).
     */
    public static void updateEntity(DirectMessageEntity entity, MessageModel model) {
        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(model, "model must not be null");

        if (model.getConversationId() != null) {
            entity.setConversationId(model.getConversationId());
        }

        if (model.getContent() != null) {
            entity.setContent(model.getContent());
        }

        entity.setRead(model.isRead());
        entity.setEdited(model.isEdited());
    }
}
