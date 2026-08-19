package logic_core.infrastructure.mapper;

import Shared.Models.BaseEntity;
import Shared.Models.Conversation.Conversation;
import Shared.Models.ConversationMember.ConversationMember;
import Shared.Models.User.User;
import jakarta.persistence.EntityManager;
import logic_core.domain.model.ConversationModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ConversationPersistenceMapper
{
    private ConversationPersistenceMapper()
    {
    }

    public static ConversationModel toModel(Conversation entity)
    {
        if (entity == null)
        {
            return null;
        }

        List<UUID> participantIds =
                entity.getMembers() == null
                        ? List.of()
                        : entity.getMembers()
                        .stream()
                        .map(ConversationMember::getUser)
                        .map(User::getId)
                        .toList();

        return ConversationModel.builder()
                .conversationId(entity.getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .participantIds(participantIds)
                .build();
    }

    /**
     * Create
     */
    public static Conversation toPersistence(ConversationModel model)
    {
        if (model == null)
        {
            return null;
        }

        Conversation conversation = new Conversation();

        // اگر ConversationModel مربوط به update بود
        // و id داشت، ست کن.
        if (model.getConversationId() != null)
        {
            try
            {
                Field idField = BaseEntity.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(conversation, model.getConversationId());
            }
            catch (ReflectiveOperationException e)
            {
                throw new IllegalStateException(e);
            }
        }

        conversation.setMembers(new ArrayList<>());
        conversation.setMessages(new ArrayList<>());

        return conversation;
    }

    /**
     * Update
     */
    public static void updateEntity(
            ConversationModel model,
            Conversation entity,
            EntityManager em)
    {
        if (model == null || entity == null)
        {
            return;
        }

        List<ConversationMember> current =
                entity.getMembers() == null
                        ? new ArrayList<>()
                        : entity.getMembers();

        Set<UUID> targetIds = new HashSet<>(
                model.getParticipantIds() == null
                        ? List.of()
                        : model.getParticipantIds()
        );

        current.removeIf(member ->
                !targetIds.contains(member.getUser().getId()));

        for (UUID userId : targetIds)
        {
            boolean exists = current.stream()
                    .anyMatch(m ->
                            m.getUser().getId().equals(userId));

            if (!exists)
            {
                ConversationMember member =
                        ConversationMember.builder()
                                .conversation(entity)
                                .user(em.getReference(User.class, userId))
                                .build();

                current.add(member);
            }
        }

        entity.setMembers(current);
    }

    private static List<UUID> extractParticipantIds(List<ConversationMember> members)
    {
        if (members == null)
        {
            return List.of();
        }

        return members.stream()
                .map(ConversationMember::getUser)
                .map(User::getId)
                .toList();
    }
}