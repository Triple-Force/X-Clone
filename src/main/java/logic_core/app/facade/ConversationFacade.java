package logic_core.app.facade;

import logic_core.app.dto.request.AddConversationMemberRequest;
import logic_core.app.dto.request.CreateConversationRequest;
import logic_core.app.dto.request.DeleteConversationRequest;
import logic_core.app.dto.request.RemoveConversationMemberRequest;
import logic_core.app.dto.response.ConversationInfoResponse;
import logic_core.app.dto.response.CreateConversationResponse;
import logic_core.app.dto.response.DeleteConversationResponse;
import logic_core.app.usecase.conversation.AddMemberToConversationUseCase;
import logic_core.app.usecase.conversation.CreateConversationUseCase;
import logic_core.app.usecase.conversation.DeleteConversationUseCase;
import logic_core.app.usecase.conversation.DeleteMemberFromConversationUseCase;
import logic_core.common.result.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConversationFacade
{
    @NonNull private final AddMemberToConversationUseCase addMemberToConversationUseCase;
    @NonNull private final CreateConversationUseCase createConversationUseCase;
    @NonNull private final DeleteConversationUseCase deleteConversationUseCase;
    @NonNull private final DeleteMemberFromConversationUseCase deleteMemberFromConversationUseCase;

    public Result<ConversationInfoResponse> addMember(AddConversationMemberRequest request)
    {
        return addMemberToConversationUseCase.execute(request);
    }

    public Result<CreateConversationResponse> createConversation(CreateConversationRequest request)
    {
        return createConversationUseCase.execute(request);
    }

    public Result<DeleteConversationResponse> deleteConversation(DeleteConversationRequest request)
    {
        return deleteConversationUseCase.execute(request);
    }

    public Result<ConversationInfoResponse> deleteMember(RemoveConversationMemberRequest request)
    {
        return deleteMemberFromConversationUseCase.execute(request);
    }
}
