package logic_core.infrastructure.mapper;

import Shared.Models.Conversation.Conversation;
import Shared.Models.DirectMessage.DirectMessage;
import Shared.Models.User.User;
import jakarta.persistence.EntityManager;
import logic_core.domain.model.MessageModel;

import java.time.ZoneOffset;
import java.util.Objects;

public final class DirectMessagePersistenceMapper
{
    private DirectMessagePersistenceMapper()
    {
    }

    public static MessageModel toDomain(DirectMessage entity)
    {
        if (entity == null) return null;

        return MessageModel.builder()
                .messageId(entity.getId())
                .conversationId(entity.getConversation() != null ? entity.getConversation().getId() : null)
                .senderId(entity.getSender() != null ? entity.getSender().getId() : null)
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .isRead(entity.isRead())
                .edited(entity.isEdited())
                .sentAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toInstant().atOffset(ZoneOffset.UTC) : null)
                .build();
    }

    public static DirectMessage toPersistence(MessageModel model, EntityManager entityManager)
    {
        Objects.requireNonNull(model, "model must not be null");
        Objects.requireNonNull(entityManager, "entityManager must not be null");
        Objects.requireNonNull(model.getConversationId(), "conversationId must not be null");
        Objects.requireNonNull(model.getSenderId(), "senderId must not be null");
        Objects.requireNonNull(model.getContent(), "content must not be null");

        DirectMessage entity = new DirectMessage();
        entity.setConversation(entityManager.getReference(Conversation.class, model.getConversationId()));
        entity.setSender(entityManager.getReference(User.class, model.getSenderId()));
        entity.setContent(model.getContent());
        entity.setRead(model.isRead());
        entity.setEdited(false);

        return entity;
    }

    public static void updateEntity(DirectMessage entity, MessageModel model, EntityManager entityManager)
    {
        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(model, "model must not be null");
        Objects.requireNonNull(entityManager, "entityManager must not be null");

        if (model.getConversationId() != null)
        {
            entity.setConversation(entityManager.getReference(Conversation.class, model.getConversationId()));
        }

        if (model.getSenderId() != null)
        {
            entity.setSender(entityManager.getReference(User.class, model.getSenderId()));
        }

        if (model.getContent() != null)
        {
            entity.setContent(model.getContent());
        }

        entity.setRead(model.isRead());
        entity.setContent(model.getContent());
        entity.setRead(model.isRead());
        entity.setEdited(model.isEdited());
    }
}