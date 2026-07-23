package Client.controllers;

import Client.ClientApplicationContext;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.logging.Logger;

public class EditProfileController {

    private static final Logger log = Logger.getLogger(EditProfileController.class.getName());

    @FXML
    private Button closeButton;

    @FXML
    private ImageView avatarPreview;

    @FXML
    private Button changeAvatarButton;

    @FXML
    private TextField displayNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextArea bioTextArea;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveButton;

    private final ClientApplicationContext context;
    private final Gson gson = new Gson();

    private File selectedAvatarFile;

    public EditProfileController(ClientApplicationContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        loadCurrentUserData();
    }

    @FXML
    void handleChangeAvatar(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) changeAvatarButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            this.selectedAvatarFile = file;
            log.info("Selected new avatar file: " + file.getAbsolutePath());
            // TODO
        }
    }

    @FXML
    void handleSave(ActionEvent event) {
        String newDisplayName = displayNameField.getText();
        String newUsername = usernameField.getText();
        String newBio = bioTextArea.getText();

        if (newDisplayName == null || newDisplayName.trim().isEmpty()) {
            return;
        }

        saveButton.setDisable(true);

        context.networkExecutor().execute(() -> {
            try {
                log.info("Saving updated profile information...");

                /*
                 * TODO
                 *
                 * User updatedUser = new User();
                 * updatedUser.setDisplayName(newDisplayName.trim());
                 * updatedUser.setUsername(newUsername.trim());
                 * // updatedUser.setBio(newBio.trim());
                 *
                 * RequestEnvelope request = new RequestEnvelope(
                 *         UUID.randomUUID(),
                 *         RequestType.USER_UPDATE_PROFILE, // Replace with actual RequestType when available
                 *         gson.toJsonTree(updatedUser),
                 *         null
                 * );
                 * ResponseEnvelope response = context.socketClient().send(request);
                 */

                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                    closeModal();
                });

            } catch (Exception e) {
                log.severe("Error saving profile changes: " + e.getMessage());
                Platform.runLater(() -> saveButton.setDisable(false));
            }
        });
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeModal();
    }

    private void loadCurrentUserData() {
        if (!context.session().isLoggedIn()) {
            return;
        }
        displayNameField.setText("Current User");
        usernameField.setText("current_user");
        bioTextArea.setText("Sample bio text...");
    }

    private void closeModal() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}