package Client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class MessagesController {

    @FXML
    private VBox chatsContainer;

    @FXML
    private VBox messagesContainer;

    @FXML
    private Label currentChatUserLabel;

    @FXML
    private TextField messageInputField;

    @FXML
    private Button sendMessageButton;

    @FXML
    void handleSendMessage(ActionEvent event) {

    }
}