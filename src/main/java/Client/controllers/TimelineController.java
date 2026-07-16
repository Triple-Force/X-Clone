package Client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import Shared.Models.Tweet.Tweet;
import Shared.Models.User.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TimelineController {

    @FXML
    private VBox tweetsContainer;

    @FXML
    private TextField searchField;

    @FXML
    private Button homeButton;

    @FXML
    private Button messagesButton;

    @FXML
    private Button profileButton;

    @FXML
    private Button postButton;

    @FXML
    void handleHomeNavigation(ActionEvent event) {

    }

    @FXML
    void handleMessagesNavigation(ActionEvent event) {

    }

    @FXML
    void handleProfileNavigation(ActionEvent event) {

    }

    @FXML
    void handleNewPost(ActionEvent event) {

    }
}
