package logic_core.app.facade;

import logic_core.app.dto.request.*;
import logic_core.app.dto.response.ConversationInfoResponse;
import logic_core.app.dto.response.CreateConversationResponse;
import logic_core.app.dto.response.DeleteConversationResponse;
import logic_core.app.dto.response.GetConversationsResponse;
import logic_core.app.usecase.conversation.*;
import logic_core.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConversationFacade
{
    private final AddMemberToConversationUseCase addMemberToConversationUseCase;
    private final CreateConversationUseCase createConversationUseCase;
    private final DeleteConversationUseCase deleteConversationUseCase;
    private final DeleteMemberFromConversationUseCase deleteMemberFromConversationUseCase;
    private final GetConversationsUseCase getConversationsUseCase;

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
