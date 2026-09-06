package logic_core.infrastructure.repository;

import logic_core.domain.model.ConversationModel;
import logic_core.infrastructure.config.AppConfig;
import logic_core.infrastructure.persistence.entity.conversation.ConversationEntity;
import logic_core.infrastructure.persistence.entity.directmessage.DirectMessageEntity;
import logic_core.infrastructure.persistence.entity.UserEntity;
import com.xclone.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ContextConfiguration(classes = {
        Application.class,
        AppConfig.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ConversationRepositoryAdapter.class)
class ConversationRepositoryAdapterIntegrationTest {

    @Autowired
    private ConversationRepositoryAdapter conversationRepositoryAdapter;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private UserEntity alice;
    private UserEntity bob;
    private UserEntity charlie;
    private UserEntity dave;
    private UserEntity erin;

    @BeforeEach
    void setUp() {
        alice = saveUser("alice");
        bob = saveUser("bob");
        charlie = saveUser("charlie");
        dave = saveUser("dave");
        erin = saveUser("erin");
    }

    @Test
    void save_shouldCreateConversationWithMembers() {
        ConversationModel saved = createConversation(alice.getId(), bob.getId());

        assertThat(saved.getConversationId()).isNotNull();
        assertThat(saved.getParticipantIds())
                .containsExactlyInAnyOrder(alice.getId(), bob.getId());

        testEntityManager.flush();

        ConversationEntity entity = testEntityManager.find(ConversationEntity.class, saved.getConversationId());
        assertThat(entity).isNotNull();
        assertThat(entity.isDeleted()).isFalse();
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();

        assertThat(countMembers(saved.getConversationId())).isEqualTo(2L);
    }

    @Test
    void findById_shouldReturnConversationWithParticipants() {
        UUID conversationId = createConversation(alice.getId(), bob.getId()).getConversationId();

        Optional<ConversationModel> found = conversationRepositoryAdapter.findById(conversationId);

        assertThat(found).isPresent();
        assertThat(found.get().getConversationId()).isEqualTo(conversationId);
        assertThat(found.get().getParticipantIds())
                .containsExactlyInAnyOrder(alice.getId(), bob.getId());
        assertThat(found.get().getCreatedAt()).isNotNull();

        assertThat(conversationRepositoryAdapter.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findByIdForUpdate_shouldReturnConversationWithParticipants() {
        UUID conversationId = createConversation(alice.getId(), bob.getId()).getConversationId();

        Optional<ConversationModel> found = conversationRepositoryAdapter.findByIdForUpdate(conversationId);

        assertThat(found).isPresent();
        assertThat(found.get().getParticipantIds())
                .containsExactlyInAnyOrder(alice.getId(), bob.getId());

        assertThat(conversationRepositoryAdapter.findByIdForUpdate(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findDirectConversationBetween_shouldMatchOnlyTwoMemberConversations() {
        UUID direct = createConversation(alice.getId(), bob.getId()).getConversationId();

        assertThat(conversationRepositoryAdapter.findDirectConversationBetween(
                alice.getId(), bob.getId())).isPresent();
        // Symmetric lookup
        assertThat(conversationRepositoryAdapter.findDirectConversationBetween(
                bob.getId(), alice.getId())).isPresent();

        // Pair without a conversation
        assertThat(conversationRepositoryAdapter.findDirectConversationBetween(
                alice.getId(), charlie.getId())).isEmpty();

        // A 3-member conversation is not a "direct" conversation
        UUID group = createConversation(alice.getId(), bob.getId(), charlie.getId()).getConversationId();
        assertThat(group).isNotNull();
        assertThat(conversationRepositoryAdapter.findDirectConversationBetween(
                alice.getId(), charlie.getId())).isEmpty();
        // The real direct conversation is still the only match for alice+bob
        Optional<ConversationModel> directLookup =
                conversationRepositoryAdapter.findDirectConversationBetween(alice.getId(), bob.getId());
        assertThat(directLookup).isPresent();
        assertThat(directLookup.get().getConversationId()).isEqualTo(direct);

        // Soft-deleted conversations are not returned
        conversationRepositoryAdapter.deleteById(direct);
        assertThat(conversationRepositoryAdapter.findDirectConversationBetween(
                alice.getId(), bob.getId())).isEmpty();
    }

    @Test
    void findConversationsByUserId_shouldPaginateWithCountParity() {
        UUID c1 = createConversation(alice.getId(), bob.getId()).getConversationId();
        sleep(2);
        UUID c2 = createConversation(alice.getId(), charlie.getId()).getConversationId();
        sleep(2);
        UUID c3 = createConversation(alice.getId(), dave.getId()).getConversationId();
        sleep(2);
        UUID c4 = createConversation(alice.getId(), erin.getId()).getConversationId();

        // Conversation that does not involve alice
        createConversation(bob.getId(), charlie.getId());

        assertThat(conversationRepositoryAdapter.countConversations(alice.getId())).isEqualTo(4L);

        List<ConversationModel> page1 = conversationRepositoryAdapter.findConversationsByUserId(alice.getId(), 1, 2);
        List<ConversationModel> page2 = conversationRepositoryAdapter.findConversationsByUserId(alice.getId(), 2, 2);

        assertThat(page1).hasSize(2);
        assertThat(page2).hasSize(2);

        // updatedAt DESC: newest (c4, c3) first, then older (c2, c1)
        assertThat(page1)
                .extracting(ConversationModel::getConversationId)
                .containsExactly(c4, c3);
        assertThat(page2)
                .extracting(ConversationModel::getConversationId)
                .containsExactly(c2, c1);

        // Every returned model carries its participants
        assertThat(page1.get(0).getParticipantIds()).contains(alice.getId());
        assertThat(page2.get(1).getParticipantIds()).contains(alice.getId());
    }

    @Test
    void addMember_shouldAddParticipant() {
        UUID conversationId = createConversation(alice.getId(), bob.getId()).getConversationId();

        conversationRepositoryAdapter.addMember(conversationId, charlie.getId());

        Optional<ConversationModel> found = conversationRepositoryAdapter.findById(conversationId);
        assertThat(found).isPresent();
        assertThat(found.get().getParticipantIds())
                .containsExactlyInAnyOrder(alice.getId(), bob.getId(), charlie.getId());
        assertThat(countMembers(conversationId)).isEqualTo(3L);
    }

    @Test
    void deleteMember_shouldRemoveParticipantRow() {
        UUID conversationId = createConversation(alice.getId(), bob.getId(), charlie.getId()).getConversationId();

        conversationRepositoryAdapter.deleteMember(conversationId, charlie.getId());

        Optional<ConversationModel> found = conversationRepositoryAdapter.findById(conversationId);
        assertThat(found).isPresent();
        assertThat(found.get().getParticipantIds())
                .containsExactlyInAnyOrder(alice.getId(), bob.getId());
        assertThat(countMembers(conversationId)).isEqualTo(2L);

        // Deleting a non-existent relation mirrors the legacy DAO (IllegalArgumentException)
        assertThatThrownBy(() -> conversationRepositoryAdapter.deleteMember(conversationId, dave.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteById_shouldSoftDeleteConversationAndHardDeleteMembersAndMessages() {
        UUID conversationId = createConversation(alice.getId(), bob.getId()).getConversationId();
        insertMessage(conversationId, alice, "hello");
        insertMessage(conversationId, bob, "hi back");
        insertMessage(conversationId, bob, "second reply");

        assertThat(countMessages(conversationId)).isEqualTo(3L);

        conversationRepositoryAdapter.deleteById(conversationId);
        testEntityManager.flush();

        ConversationEntity entity = testEntityManager.find(ConversationEntity.class, conversationId);
        assertThat(entity).isNotNull();
        assertThat(entity.isDeleted()).isTrue();

        assertThat(countMembers(conversationId)).isZero();
        assertThat(countMessages(conversationId)).isZero();
    }

    @Test
    void deletedConversation_shouldBeHiddenFromAllReads() {
        UUID conversationId = createConversation(alice.getId(), bob.getId()).getConversationId();

        conversationRepositoryAdapter.deleteById(conversationId);

        assertThat(conversationRepositoryAdapter.findById(conversationId)).isEmpty();
        assertThat(conversationRepositoryAdapter.findByIdForUpdate(conversationId)).isEmpty();
        assertThat(conversationRepositoryAdapter.existsById(conversationId)).isFalse();
        assertThat(conversationRepositoryAdapter.countConversations(alice.getId())).isZero();
        assertThat(conversationRepositoryAdapter.findConversationsByUserId(alice.getId(), 1, 10)).isEmpty();
    }

    @Test
    void softDeletedUserMembership_shouldBeExcludedFromListingsButKeptInMemberMaterialization() {
        UUID conversationId = createConversation(alice.getId(), bob.getId()).getConversationId();

        // Soft-delete bob (the migrated user delete only marks is_deleted; member rows remain)
        bob.markDeleted();
        userJpaRepository.save(bob);
        testEntityManager.flush();

        // List/count/direct-between queries filter on user.isDeleted (legacy parity)
        assertThat(conversationRepositoryAdapter.findConversationsByUserId(bob.getId(), 1, 10)).isEmpty();
        assertThat(conversationRepositoryAdapter.countConversations(bob.getId())).isZero();
        assertThat(conversationRepositoryAdapter.findDirectConversationBetween(
                alice.getId(), bob.getId())).isEmpty();

        // alice still sees the conversation
        assertThat(conversationRepositoryAdapter.countConversations(alice.getId())).isEqualTo(1L);
        assertThat(conversationRepositoryAdapter.findConversationsByUserId(alice.getId(), 1, 10))
                .extracting(ConversationModel::getConversationId)
                .containsExactly(conversationId);

        // findById materializes the membership row as-is (legacy findById never
        // filtered members by user deletion)
        Optional<ConversationModel> found = conversationRepositoryAdapter.findById(conversationId);
        assertThat(found).isPresent();
        assertThat(found.get().getParticipantIds())
                .containsExactlyInAnyOrder(alice.getId(), bob.getId());
    }

    private ConversationModel createConversation(UUID... participantIds) {
        return conversationRepositoryAdapter.save(ConversationModel.builder()
                .participantIds(List.of(participantIds))
                .build());
    }

    private DirectMessageEntity insertMessage(UUID conversationId, UserEntity sender, String content) {
        DirectMessageEntity message = new DirectMessageEntity();
        message.setConversationId(conversationId);
        message.setSender(sender);
        message.setContent(content);
        return testEntityManager.persistFlushFind(message);
    }

    private long countMembers(UUID conversationId) {
        return testEntityManager.getEntityManager()
                .createQuery("SELECT COUNT(cm) FROM ConversationMemberEntity cm WHERE cm.conversation.id = :cid", Long.class)
                .setParameter("cid", conversationId)
                .getSingleResult();
    }

    private long countMessages(UUID conversationId) {
        return testEntityManager.getEntityManager()
                .createQuery("SELECT COUNT(dm) FROM DirectMessageEntity dm WHERE dm.conversationId = :cid", Long.class)
                .setParameter("cid", conversationId)
                .getSingleResult();
    }

    private UserEntity saveUser(String username) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("hash");
        return userJpaRepository.save(user);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
