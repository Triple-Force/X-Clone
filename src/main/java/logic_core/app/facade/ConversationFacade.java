package logic_core.app.facade;

import logic_core.app.dto.request.*;
import logic_core.app.dto.response.ConversationInfoResponse;
import logic_core.app.dto.response.CreateConversationResponse;
import logic_core.app.dto.response.DeleteConversationResponse;
import logic_core.app.dto.response.GetConversationsResponse;
import logic_core.app.usecase.conversation.*;
import logic_core.app.usecase.message.GetConversationMessagesUseCase;
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
    @NonNull private final GetConversationsUseCase getConversationsUseCase;

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

    public Result<GetConversationsResponse> getConversations(GetConversationsRequest request)
    {
        return getConversationsUseCase.execute(request);
    }
}
