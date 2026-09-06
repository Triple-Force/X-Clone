package logic_core.domain.policy;

import logic_core.common.exception.ConflictException;
import logic_core.common.exception.ForbiddenException;
import logic_core.common.exception.NotFoundException;
import logic_core.common.exception.ValidationException;
import logic_core.domain.model.ConversationModel;
import logic_core.domain.repository.ConversationRepository;
import logic_core.domain.repository.RelationshipRepository;
import logic_core.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConversationPolicy
{
    private static final int MIN_MEMBERS = 2;
    private static final int MAX_MEMBERS = 50;

    private final UserRepository userRepository;
    private final RelationshipRepository relationshipRepository;
    private final ConversationRepository conversationRepository;

    public void validateCreate(UUID creatorId, List<UUID> participantIds)
    {
        if (!userRepository.existsById(creatorId))
        {
            throw new NotFoundException("Creator not found.");
        }

        validateParticipantsExist(participantIds);
        validateConversationSize(creatorId, participantIds);
        validateCreatorBlockRules(creatorId, participantIds);
        validateDirectConversationDuplicate(creatorId, participantIds);
    }

    public void validateDelete(UUID actorId, UUID conversationId)
    {
        ConversationModel conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found."));

        if (!conversation.getParticipantIds().contains(actorId))
        {
            throw new ForbiddenException("Actor is not a participant of this conversation.");
        }
    }

    public void validateAddMember(UUID actorId,
                                  UUID memberId,
                                  UUID conversationId)
    {
        if (!userRepository.existsById(actorId))
        {
            throw new NotFoundException("Actor not found.");
        }

        if (!userRepository.existsById(memberId))
        {
            throw new NotFoundException("Member not found.");
        }

        ConversationModel conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() ->
                        new NotFoundException("Conversation not found."));

        if (!conversation.getParticipantIds().contains(actorId))
        {
            throw new ForbiddenException(
                    "Only conversation participants can add new members.");
        }

        if (conversation.getParticipantIds().contains(memberId))
        {
            throw new ConflictException(
                    "Member is already in this conversation.");
        }

        for (UUID participantId : conversation.getParticipantIds())
        {
            if (isBlockedEitherSide(memberId, participantId))
            {
                throw new ForbiddenException(
                        "Cannot add member because a block relationship exists.");
            }
        }

        if (conversation.getParticipantIds().size() >= MAX_MEMBERS)
        {
            throw new ValidationException(
                    "Conversation cannot exceed " + MAX_MEMBERS + " members.");
        }
    }

    public void validateDeleteMember(UUID actorId,
                                     UUID memberId,
                                     UUID conversationId)
    {
        if (!userRepository.existsById(actorId))
        {
            throw new NotFoundException("Actor not found.");
        }

        if (!userRepository.existsById(memberId))
        {
            throw new NotFoundException("Member not found.");
        }

        ConversationModel conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() ->
                        new NotFoundException("Conversation not found."));

        if (!conversation.getParticipantIds().contains(actorId))
        {
            throw new ForbiddenException(
                    "Only conversation participants can remove members.");
        }

        if (!conversation.getParticipantIds().contains(memberId))
        {
            throw new ConflictException(
                    "Member is not part of this conversation.");
        }

        if (conversation.getParticipantIds().size() <= MIN_MEMBERS)
        {
            throw new ValidationException(
                    "Conversation must contain at least "
                            + MIN_MEMBERS
                            + " participants.");
        }
    }

    private void validateParticipantsExist(List<UUID> participantIds)
    {
        for (UUID participantId : participantIds)
        {
            if (!userRepository.existsById(participantId))
            {
                throw new NotFoundException("Participant not found: " + participantId);
            }
        }
    }

    private void validateConversationSize(UUID creatorId, List<UUID> participantIds)
    {
        Set<UUID> allMembers = new HashSet<>();
        allMembers.add(creatorId);
        allMembers.addAll(participantIds);

        int size = allMembers.size();

        if (size < MIN_MEMBERS)
        {
            throw new ValidationException("Conversation must have at least " + MIN_MEMBERS + " members.");
        }

        if (size > MAX_MEMBERS)
        {
            throw new ValidationException("Conversation must not exceed " + MAX_MEMBERS + " members.");
        }
    }

    private void validateCreatorBlockRules(UUID creatorId, List<UUID> participantIds)
    {
        for (UUID participantId : participantIds)
        {
            if (isBlockedEitherSide(creatorId, participantId))
            {
                throw new ForbiddenException(
                        "Conversation cannot be created because a block relationship exists between creator and participant: "
                                + participantId
                );
            }
        }
    }

    private void validateDirectConversationDuplicate(UUID creatorId, List<UUID> participantIds)
    {
        if (participantIds.size() != 1)
        {
            return;
        }

        UUID otherParticipantId = participantIds.get(0);

        conversationRepository.findDirectConversationBetween(creatorId, otherParticipantId)
                .ifPresent(existing -> {
                    throw new ConflictException(
                            "Direct conversation already exists: " + existing.getConversationId()
                    );
                });
    }

    private boolean isBlockedEitherSide(UUID firstUserId, UUID secondUserId)
    {
        return relationshipRepository.isBlockedBy(firstUserId, secondUserId)
                || relationshipRepository.isBlockedBy(secondUserId, firstUserId);
    }
}