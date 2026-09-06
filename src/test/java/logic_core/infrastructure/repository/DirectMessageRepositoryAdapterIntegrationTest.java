package logic_core.infrastructure.repository;

import logic_core.domain.model.MessageModel;
import logic_core.infrastructure.config.AppConfig;
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

@DataJpaTest
@ContextConfiguration(classes = {
        Application.class,
        AppConfig.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(DirectMessageRepositoryAdapter.class)
class DirectMessageRepositoryAdapterIntegrationTest {

    @Autowired
    private DirectMessageRepositoryAdapter directMessageRepositoryAdapter;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private UserEntity sender;
    private UserEntity receiver;
    private UUID conversationId;

    @BeforeEach
    void setUp() {
        sender = saveUser("sender");
        receiver = saveUser("receiver");
        conversationId = createConversation();
    }

    @Test
    void save_shouldPersistMessageAndReturnGeneratedModel() {
        MessageModel model = MessageModel.builder()
                .conversationId(conversationId)
                .senderId(sender.getId())
                .content("Hello from sender")
                .build();

        MessageModel saved = directMessageRepositoryAdapter.save(model);
        testEntityManager.flush();

        assertThat(saved.getMessageId()).isNotNull();
        assertThat(saved.getConversationId()).isEqualTo(conversationId);
        assertThat(saved.getSenderId()).isEqualTo(sender.getId());
        assertThat(saved.getContent()).isEqualTo("Hello from sender");

        DirectMessageEntity persisted = testEntityManager.find(DirectMessageEntity.class, saved.getMessageId());
        assertThat(persisted).isNotNull();
        assertThat(persisted.getConversationId()).isEqualTo(conversationId);
        assertThat(persisted.getSender().getId()).isEqualTo(sender.getId());
        assertThat(persisted.getContent()).isEqualTo("Hello from sender");
        assertThat(persisted.isRead()).isFalse();
        assertThat(persisted.isEdited()).isFalse();
        assertThat(persisted.isDeleted()).isFalse();
        assertThat(persisted.getCreatedAt()).isNotNull();

        // created_at is assigned at flush time (same as the legacy persist path), so a
        // freshly-read model carries the populated timestamps.
        MessageModel reloaded = directMessageRepositoryAdapter.findById(saved.getMessageId()).orElseThrow();
        assertThat(reloaded.getCreatedAt()).isNotNull();
        assertThat(reloaded.getSentAt()).isNotNull();
    }

    @Test
    void findById_shouldReturnOnlyActiveMessages() {
        DirectMessageEntity active = persistMessage(sender, "active message");
        DirectMessageEntity deleted = persistMessage(sender, "to be deleted");
        directMessageRepositoryAdapter.softDelete(deleted.getId());

        Optional<MessageModel> found = directMessageRepositoryAdapter.findById(active.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo("active message");
        assertThat(found.get().getSenderId()).isEqualTo(sender.getId());

        assertThat(directMessageRepositoryAdapter.findById(deleted.getId())).isEmpty();
    }

    @Test
    void findByIdForUpdate_shouldReturnMessageOrEmpty() {
        DirectMessageEntity message = persistMessage(sender, "lock me");

        Optional<MessageModel> found = directMessageRepositoryAdapter.findByIdForUpdate(message.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getMessageId()).isEqualTo(message.getId());

        assertThat(directMessageRepositoryAdapter.findByIdForUpdate(UUID.randomUUID())).isEmpty();

        DirectMessageEntity deleted = persistMessage(sender, "locked but deleted");
        directMessageRepositoryAdapter.softDelete(deleted.getId());
        assertThat(directMessageRepositoryAdapter.findByIdForUpdate(deleted.getId())).isEmpty();
    }

    @Test
    void softDelete_shouldRedactContentAndHideMessage() {
        DirectMessageEntity message = persistMessage(sender, "secret content");
        testEntityManager.clear();

        directMessageRepositoryAdapter.softDelete(message.getId());
        testEntityManager.flush();

        assertThat(directMessageRepositoryAdapter.findById(message.getId())).isEmpty();

        DirectMessageEntity persisted = testEntityManager.find(DirectMessageEntity.class, message.getId());
        assertThat(persisted).isNotNull();
        assertThat(persisted.isDeleted()).isTrue();
        assertThat(persisted.getContent()).isEqualTo("This message has been deleted.");
    }

    @Test
    void update_shouldChangeContent() {
        DirectMessageEntity message = persistMessage(sender, "original content");
        testEntityManager.clear();

        MessageModel updated = MessageModel.builder()
                .messageId(message.getId())
                .conversationId(conversationId)
                .senderId(sender.getId())
                .content("edited content")
                .build();

        directMessageRepositoryAdapter.update(updated);
        testEntityManager.flush();

        DirectMessageEntity persisted = testEntityManager.find(DirectMessageEntity.class, message.getId());
        assertThat(persisted.getContent()).isEqualTo("edited content");
        assertThat(persisted.isDeleted()).isFalse();
    }

    @Test
    void findByConversationId_shouldReturnActiveMessagesOldestFirst() {
        DirectMessageEntity first = persistMessage(sender, "first");
        DirectMessageEntity second = persistMessage(sender, "second");
        DirectMessageEntity third = persistMessage(sender, "third");
        DirectMessageEntity deleted = persistMessage(sender, "deleted");
        directMessageRepositoryAdapter.softDelete(deleted.getId());

        List<MessageModel> messages = directMessageRepositoryAdapter.findByConversationId(conversationId);

        assertThat(messages)
                .extracting(MessageModel::getMessageId)
                .containsExactly(first.getId(), second.getId(), third.getId());

        assertThat(messages).extracting(MessageModel::getContent)
                .containsExactly("first", "second", "third");
    }

    @Test
    void findByConversationIdForUpdate_shouldReturnAllActiveMessages() {
        DirectMessageEntity first = persistMessage(sender, "one");
        DirectMessageEntity second = persistMessage(sender, "two");

        List<MessageModel> messages = directMessageRepositoryAdapter.findByConversationIdForUpdate(conversationId);

        assertThat(messages)
                .extracting(MessageModel::getMessageId)
                .containsExactly(first.getId(), second.getId());
    }

    @Test
    void findLatestMessages_shouldReturnNewestFirstWithLimit() {
        DirectMessageEntity first = persistMessage(sender, "oldest");
        DirectMessageEntity second = persistMessage(sender, "middle");
        DirectMessageEntity third = persistMessage(sender, "newest");

        List<MessageModel> latestTwo = directMessageRepositoryAdapter.findLatestMessages(conversationId, 2);
        assertThat(latestTwo)
                .extracting(MessageModel::getMessageId)
                .containsExactly(third.getId(), second.getId());

        List<MessageModel> latestOne = directMessageRepositoryAdapter.findLatestMessages(conversationId, 1);
        assertThat(latestOne)
                .extracting(MessageModel::getMessageId)
                .containsExactly(third.getId());

        // Soft-deleted messages never appear
        directMessageRepositoryAdapter.softDelete(first.getId());
        List<MessageModel> latestThree = directMessageRepositoryAdapter.findLatestMessages(conversationId, 3);
        assertThat(latestThree)
                .extracting(MessageModel::getMessageId)
                .containsExactly(third.getId(), second.getId());
    }

    @Test
    void countUnreadMessages_shouldCountOnlyOthersActiveUnreadMessages() {
        // Unread message from the sender -> counted for the receiver
        persistMessage(sender, "unread");

        // Read message from the sender -> not counted
        DirectMessageEntity readMessage = new DirectMessageEntity();
        readMessage.setConversationId(conversationId);
        readMessage.setSender(sender);
        readMessage.setContent("already read");
        readMessage.setRead(true);
        testEntityManager.persistFlushFind(readMessage);

        // Soft-deleted message from the sender -> not counted
        DirectMessageEntity deletedMessage = persistMessage(sender, "deleted unread");
        directMessageRepositoryAdapter.softDelete(deletedMessage.getId());

        // The receiver's own message -> not counted
        persistMessage(receiver, "my own message");

        // Message from a soft-deleted user -> not counted
        UserEntity deletedSender = saveUser("deletedSender");
        deletedSender.markDeleted();
        userJpaRepository.save(deletedSender);
        persistMessage(deletedSender, "from deleted user");

        long unread = directMessageRepositoryAdapter.countUnreadMessages(conversationId, receiver.getId());
        assertThat(unread).isEqualTo(1L);
    }

    @Test
    void findByConversationId_shouldApplyPageAndPageSize() {
        DirectMessageEntity first = persistMessage(sender, "one");
        DirectMessageEntity second = persistMessage(sender, "two");
        DirectMessageEntity third = persistMessage(sender, "three");

        // Historical runtime semantics: (conversationId, page, pageSize)
        List<MessageModel> firstPage = directMessageRepositoryAdapter.findByConversationId(conversationId, 1, 2);
        assertThat(firstPage)
                .extracting(MessageModel::getMessageId)
                .containsExactly(first.getId(), second.getId());

        List<MessageModel> secondPage = directMessageRepositoryAdapter.findByConversationId(conversationId, 2, 2);
        assertThat(secondPage)
                .extracting(MessageModel::getMessageId)
                .containsExactly(third.getId());
    }

    @Test
    void findLastMessage_shouldReturnNewestActiveOrEmpty() {
        assertThat(directMessageRepositoryAdapter.findLastMessage(conversationId)).isEmpty();

        persistMessage(sender, "first");
        persistMessage(sender, "second");

        Optional<MessageModel> last = directMessageRepositoryAdapter.findLastMessage(conversationId);
        assertThat(last).isPresent();
        assertThat(last.get().getContent()).isEqualTo("second");

        DirectMessageEntity lastEntity = persistMessage(sender, "third");
        directMessageRepositoryAdapter.softDelete(lastEntity.getId());

        Optional<MessageModel> afterDelete = directMessageRepositoryAdapter.findLastMessage(conversationId);
        assertThat(afterDelete).isPresent();
        assertThat(afterDelete.get().getContent()).isEqualTo("second");
    }

    private UserEntity saveUser(String username) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("hash");
        return userJpaRepository.save(user);
    }

    private UUID createConversation() {
        UUID id = UUID.randomUUID();
        testEntityManager.getEntityManager()
                .createNativeQuery("""
                        INSERT INTO conversations (id, created_at, updated_at, is_deleted)
                        VALUES (:id, now(), now(), false)
                        """)
                .setParameter("id", id)
                .executeUpdate();
        return id;
    }

    private DirectMessageEntity persistMessage(UserEntity from, String content) {
        DirectMessageEntity message = new DirectMessageEntity();
        message.setConversationId(conversationId);
        message.setSender(from);
        message.setContent(content);
        message = testEntityManager.persistFlushFind(message);
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return message;
    }
}
