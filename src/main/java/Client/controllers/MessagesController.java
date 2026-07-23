package Client.controllers;

import Client.ClientApplicationContext;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class MessagesController {

    private static final Logger log = Logger.getLogger(MessagesController.class.getName());

    @FXML
    private VBox chatsContainer;

    @FXML
    private Label currentChatUserLabel;

    @FXML
    private VBox messagesContainer;

    @FXML
    private TextField messageInputField;

    @FXML
    private Button sendMessageButton;

    private final ClientApplicationContext context;
    private final Gson gson = new Gson();

    private UUID selectedChatId;

    public MessagesController(ClientApplicationContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        loadUserConversations();
    }

    @FXML
    void handleSendMessage(ActionEvent event) {
        String messageText = messageInputField.getText();

        if (messageText == null || messageText.trim().isEmpty()) {
            return;
        }

        if (selectedChatId == null) {
            log.warning("No conversation selected to send message.");
            return;
        }

        sendMessageButton.setDisable(true);

        context.networkExecutor().execute(() -> {
            try {
                log.info("Sending message locally to chat " + selectedChatId + ": " + messageText);

                /*
                 * TODO
                 *
                 * MessageEnvelope payload = new MessageEnvelope(selectedChatId, messageText.trim());
                 * RequestEnvelope request = new RequestEnvelope(
                 *         UUID.randomUUID(),
                 *         RequestType.MESSAGE_SEND, // Replace with actual RequestType when available
                 *         gson.toJsonTree(payload),
                 *         null
                 * );
                 * ResponseEnvelope response = context.socketClient().send(request);
                 */

                Platform.runLater(() -> {
                    messageInputField.clear();
                    sendMessageButton.setDisable(false);
                    // Refresh current active conversation messages
                    loadConversationMessages(selectedChatId);
                });

            } catch (Exception e) {
                log.severe("Error sending message: " + e.getMessage());
                Platform.runLater(() -> sendMessageButton.setDisable(false));
            }
        });
    }

    private void loadUserConversations() {
        context.networkExecutor().execute(() -> {
            try {
                /*
                 * TODO
                 *
                 * RequestEnvelope request = new RequestEnvelope(
                 *         UUID.randomUUID(),
                 *         RequestType.MESSAGE_GET_CONVERSATIONS, // Replace with actual RequestType when available
                 *         null,
                 *         null
                 * );
                 * ResponseEnvelope response = context.socketClient().send(request);
                 */


                List<String> mockChatUsers = List.of();

                Platform.runLater(() -> {
                    chatsContainer.getChildren().clear();

                    for (String username : mockChatUsers) {
                        Label chatItem = new Label(username);
                        chatItem.setStyle("-fx-padding: 10; -fx-cursor: hand; -fx-font-size: 14px;");

                        chatItem.setOnMouseClicked(e -> {
                            selectedChatId = UUID.randomUUID();
                            currentChatUserLabel.setText(username);
                            loadConversationMessages(selectedChatId);
                        });

                        chatsContainer.getChildren().add(chatItem);
                    }
                });

            } catch (Exception e) {
                log.severe("Error loading conversations: " + e.getMessage());
            }
        });
    }

    private void loadConversationMessages(UUID chatId) {
        if (chatId == null) {
            return;
        }

        context.networkExecutor().execute(() -> {
            try {
                /*
                 * TODO
                 *
                 * RequestEnvelope request = new RequestEnvelope(
                 *         UUID.randomUUID(),
                 *         RequestType.MESSAGE_GET_HISTORY, // Replace with actual RequestType when available
                 *         gson.toJsonTree(chatId),
                 *         null
                 * );
                 * ResponseEnvelope response = context.socketClient().send(request);
                 */

                // Temporary mock messages for testing conversation UI layout
                List<String> mockMessages = List.of("");

                Platform.runLater(() -> {
                    messagesContainer.getChildren().clear();

                    for (String msg : mockMessages) {
                        Label msgLabel = new Label(msg);
                        msgLabel.setStyle("-fx-background-color: #eff3f4; -fx-padding: 8 12 8 12; -fx-background-radius: 12;");
                        messagesContainer.getChildren().add(msgLabel);
                    }
                });

            } catch (Exception e) {
                log.severe("Error loading conversation messages: " + e.getMessage());
            }
        });
    }
}