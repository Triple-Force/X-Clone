package logic_core.infrastructure.repository;

import Shared.Database.EntityManagerContext;
import Shared.Models.Conversation.Conversation;
import Shared.Models.ConversationMember.ConversationMember;
import Shared.Models.User.User;
import jakarta.persistence.EntityManager;
import logic_core.common.exception.NotFoundException;
import logic_core.domain.model.ConversationModel;
import logic_core.domain.repository.ConversationRepository;
import logic_core.infrastructure.dao.ConversationDao;
import logic_core.infrastructure.dao.ConversationMemberDao;
import logic_core.infrastructure.mapper.ConversationPersistenceMapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class JpaConversationRepository implements ConversationRepository
{
    private final ConversationDao conversationDao;
    private final ConversationMemberDao conversationMemberDao;
    private final EntityManager em;

    public JpaConversationRepository(ConversationDao conversationDao, ConversationMemberDao conversationMemberDao, EntityManager entityManager)
    {
        this.conversationDao = Objects.requireNonNull(conversationDao, "conversationDao must not be null.");
        this.conversationMemberDao = Objects.requireNonNull(conversationMemberDao, "ConversationMemberDao must not be null.");
        this.em = Objects.requireNonNull(entityManager, "entityManager must not be null.");
    }

    private EntityManager getEntityManager()
    {
        return EntityManagerContext.get();
    }

    @Override
    public Optional<ConversationModel> findById(UUID conversationId)
    {
        if (conversationId == null)
        {
            return Optional.empty();
        }

        return conversationDao.findByIdWithMembers(conversationId)
                .map(ConversationPersistenceMapper::toModel);
    }

    @Override
    public List<ConversationModel> findConversationsByUserId(UUID userId, int page, int pageSize)
    {
        if (userId == null)
        {
            return List.of();
        }

        return conversationDao.findConversationsByUserId(userId, page, pageSize).stream()
                .map(ConversationPersistenceMapper::toModel)
                .toList();
    }

    @Override
    public long countConversations(UUID userId)
    {

        return conversationDao.countConversations(userId);
    }

    @Override
    public ConversationModel save(ConversationModel model)
    {
        //-----------------------------------------
        // Persist Conversation
        //-----------------------------------------

        Conversation conversation =
                ConversationPersistenceMapper.toPersistence(model);

        conversationDao.save(conversation);

        //-----------------------------------------
        // Persist Members
        //-----------------------------------------

        for (UUID participantId : model.getParticipantIds())
        {
            User user = em.getReference(User.class, participantId);

            ConversationMember member =
                    ConversationMember.builder()
                            .conversation(conversation)
                            .user(user)
                            .build();

            conversationMemberDao.save(member);
        }

        //-----------------------------------------
        // Reload with members
        //-----------------------------------------

        Conversation persisted =
                conversationDao.findByIdWithMembers(conversation.getId())
                        .orElseThrow();

        return ConversationPersistenceMapper.toModel(persisted);
    }

    @Override
    public void update(ConversationModel model)
    {
        conversationDao.findById(model.getConversationId()).ifPresent(entity -> {
            ConversationPersistenceMapper.updateEntity(model, entity, em);
            conversationDao.updateConversation(entity);
        });
    }

    @Override
    public Optional<ConversationModel> findDirectConversationBetween(UUID firstUserId, UUID secondUserId)
    {
        return Optional.ofNullable(
                ConversationPersistenceMapper.toModel(
                        conversationDao.findDirectConversationBetween(firstUserId, secondUserId)
                )
        );
    }


    @Override
    public void deleteById(UUID conversationId)
    {
        if (conversationId == null)
        {
            return;
        }

        conversationDao.findById(conversationId)
                .ifPresent(conversationDao::delete);
    }

    @Override
    public boolean existsById(UUID conversationId)
    {
        if (conversationId == null)
        {
            return false;
        }

        return conversationDao.findById(conversationId).isPresent();
    }

    @Override
    public void deleteMember(UUID conversationId,
                             UUID memberId)
    {
        conversationMemberDao.deleteMember(
                conversationId,
                memberId
        );
    }


    @Override
    public void addMember(UUID conversationId, UUID memberId)
    {
        Conversation conversation = em.getReference(Conversation.class , conversationId);
        User user = em.getReference(User.class, memberId);

        ConversationMember relation = ConversationMember.builder()
                .conversation(conversation)
                .user(user)
                .build();

        conversationMemberDao.save(relation);
    }

    public Optional<ConversationModel> findByIdForUpdate(UUID id)
    {
        return conversationDao.findByIdForUpdate(id)
                .map(ConversationPersistenceMapper::toModel);
    }
}