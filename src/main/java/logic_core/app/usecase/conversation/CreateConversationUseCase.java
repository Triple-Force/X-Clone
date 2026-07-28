package logic_core.app.usecase.conversation;

import Shared.Models.Conversation.Conversation;
import jakarta.transaction.Transactional;
import logic_core.app.dto.request.CreateConversationRequest;
import logic_core.app.dto.response.CreateConversationResponse;
import logic_core.app.dto.validator.ConversationValidator;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.CurrentAuthContext;
import logic_core.app.security.SessionUserContext;
import logic_core.common.exception.*;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.conversation.ConversationCreatedEvent;
import logic_core.domain.model.ConversationModel;
import logic_core.domain.policy.ConversationPolicy;
import logic_core.domain.repository.ConversationRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class CreateConversationUseCase
{
    @NonNull private final ConversationValidator validator;
    @NonNull private final ConversationPolicy policy;
    @NonNull private final ConversationRepository repository;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<CreateConversationResponse> execute(CreateConversationRequest request)
    {
        try
        {
            SessionUserContext context =
                lockOrchestrator.lockAndGetContextByToken(
                        request.sessionToken()
                );

            UUID creatorId = context.lockedUser().getId();

            validator.validateCreate(creatorId, request.participantIds());
            policy.validateCreate(creatorId, request.participantIds());

            OffsetDateTime now = timeProvider.now();
            List<UUID> members = new ArrayList<>();
            members.add(creatorId);
            members.addAll(request.participantIds());

            ConversationModel conversationModel = ConversationModel.builder()
                    .createdAt(now)
                    .updatedAt(now)
                    .participantIds(List.copyOf(members))
                    .build();

            ConversationModel persistedConversation = repository.save(conversationModel);

            eventPublisher.publish(new ConversationCreatedEvent(
                    persistedConversation.getConversationId(),
                    creatorId,
                    persistedConversation.getParticipantIds(),
                    now
            ));

            return Result.success(new CreateConversationResponse(persistedConversation.getConversationId()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }
}
