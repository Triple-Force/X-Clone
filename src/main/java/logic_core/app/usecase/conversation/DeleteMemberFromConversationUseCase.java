package logic_core.app.usecase.conversation;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.RemoveConversationMemberRequest;
import logic_core.app.dto.response.ConversationInfoResponse;
import logic_core.app.dto.validator.ConversationValidator;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.CurrentAuthContext;
import logic_core.app.security.SessionUserContext;
import logic_core.common.exception.*;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.conversation.MemberDeletedFormConversationEvent;
import logic_core.domain.model.ConversationModel;
import logic_core.domain.policy.ConversationPolicy;
import logic_core.domain.repository.ConversationRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class DeleteMemberFromConversationUseCase
{
    @NonNull private final ConversationValidator validator;
    @NonNull private final ConversationPolicy policy;
    @NonNull private final ConversationRepository repository;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<ConversationInfoResponse> execute(RemoveConversationMemberRequest request)
    {
        try
        {
            SessionUserContext context =
                    lockOrchestrator.lockAndGetContextByToken(
                            request.sessionToken()
                    );

            UUID actorId = context.lockedUser().getId();

            ConversationModel conversation = repository.findByIdForUpdate(request.conversationId())
                    .orElseThrow(() -> new NotFoundException("Conversation not found."));

            validator.validateDeleteMember(actorId, request.memberId(), conversation.getConversationId());
            policy.validateDeleteMember(actorId, request.memberId(), conversation.getConversationId());

            repository.deleteMember(conversation.getConversationId(), request.memberId());

            eventPublisher.publish(new MemberDeletedFormConversationEvent(
                    conversation.getConversationId(),
                    request.memberId(),
                    actorId,
                    timeProvider.now()
            ));

            return Result.success(new ConversationInfoResponse(
                    conversation.getConversationId(),
                    conversation.getCreatedAt(),
                    timeProvider.now(),
                    conversation.getParticipantIds()
            ));
        }
        catch (Exception e)
        {
            return Result.failure(e.getMessage());
        }
    }
}
