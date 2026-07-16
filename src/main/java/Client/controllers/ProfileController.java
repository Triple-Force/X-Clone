package Client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import Shared.Models.Tweet.Tweet;
import Shared.Models.User.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProfileController {

    @FXML
    private Label headerNameLabel;

    @FXML
    private Label displayNameLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label bioLabel;

    @FXML
    private ImageView profileAvatar;

    @FXML
    private Button editProfileButton;

    @FXML
    private VBox userTweetsContainer;

    @FXML
    void handleEditProfile(ActionEvent event) {
    }

}