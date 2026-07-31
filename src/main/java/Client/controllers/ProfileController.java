package Client.controllers;

import Client.ClientApplicationContext;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logic_core.app.dto.request.GetTimelineResponse;
import logic_core.app.dto.response.ProfileInfoResponse;
import logic_core.app.dto.timeline.TimelineTweet;
import logic_core.domain.repository.TimelineType;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Logger;

public class ProfileController {

    private static final Logger log = Logger.getLogger(ProfileController.class.getName());

    @FXML private Label followersCountLabel;
    @FXML private Label followingCountLabel;
    @FXML private Label headerNameLabel;
    @FXML private ImageView profileAvatar;
    @FXML private Button editProfileButton;
    @FXML private Label displayNameLabel;
    @FXML private Label usernameLabel;
    @FXML private Label bioLabel;
    @FXML private VBox userTweetsContainer;

    private final ClientApplicationContext context;
    private static final String DEFAULT_AVATAR_RESOURCE = "/Client/images/default-avatar.png";

    private UUID profileUserId;
    private String currentUsername;

    public ProfileController(ClientApplicationContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        if (context == null || context.session() == null || !context.session().isLoggedIn()) {
            return;
        }

        this.profileUserId = context.session().getCurrentUserId();

        loadUserProfileData();
        loadUserTweets();
    }

    private void loadUserProfileData() {
        context.getUserClientService()
                .getProfile(profileUserId)
                .thenAccept(result -> Platform.runLater(() -> {
                    if (result == null || result.isFailure()) {
                        log.warning("Profile loading failed : " + (result == null ? "null" : result.getError()));
                        return;
                    }

                    ProfileInfoResponse profile = result.getData();
                    if (profile == null) return;

                    this.currentUsername = profile.username();

                    headerNameLabel.setText(safe(profile.displayName()));
                    displayNameLabel.setText(safe(profile.displayName()));
                    usernameLabel.setText(profile.username() == null ? "" : "@" + profile.username());
                    bioLabel.setText(safe(profile.bio()));

                    followersCountLabel.setText(String.valueOf(profile.followers()));
                    followingCountLabel.setText(String.valueOf(profile.following()));

                    loadAvatar(profile);
                }))
                .exceptionally(error -> {
                    log.severe("Profile exception : " + error.getMessage());
                    return null;
                });
    }

    private void loadAvatar(ProfileInfoResponse profile) {
        try {
            if (profile != null && profile.avatarUrl() != null && !profile.avatarUrl().isBlank()) {
                profileAvatar.setImage(new Image(profile.avatarUrl(), true));
            } else {
                setDefaultAvatar();
            }
        } catch (Exception ignored) {
            setDefaultAvatar();
        }
    }

    private void setDefaultAvatar() {
        try {
            URL resource = getClass().getResource(DEFAULT_AVATAR_RESOURCE);
            if (resource != null) {
                profileAvatar.setImage(new Image(resource.toExternalForm(), true));
            }
        } catch (Exception ignored) {}
    }

    private void loadUserTweets() {
        context.getTimelineService()
                .getTimeline(
                        TimelineType.USER,
                        profileUserId,
                        profileUserId,
                        0,
                        20
                )
                .thenAccept(result -> Platform.runLater(() -> {
                    userTweetsContainer.getChildren().clear();

                    if (result == null || result.isFailure()) {
                        showEmptyState("Unable to load tweets");
                        return;
                    }

                    GetTimelineResponse response = result.getData();
                    if (response == null || response.tweets() == null || response.tweets().isEmpty()) {
                        showEmptyState("No tweets yet");
                        return;
                    }

                    for (TimelineTweet tweet : response.tweets()) {
                        addTweetCard(tweet);
                    }
                }))
                .exceptionally(error -> {
                    Platform.runLater(() -> showEmptyState("Something went wrong"));
                    return null;
                });
    }

    private void addTweetCard(TimelineTweet tweet) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client/fxml/TweetItem.fxml"));

            loader.setControllerFactory(type -> {
                if (type == TweetItemController.class) {
                    return new TweetItemController(context);
                }
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Node card = loader.load();
            TweetItemController controller = loader.getController();
            controller.setTweet(tweet);

            userTweetsContainer.getChildren().add(card);

        } catch (IOException e) {
            log.severe("Tweet card error : " + e.getMessage());
        }
    }

    @FXML
    private void handleEditProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client/fxml/EditProfile.fxml"));

            loader.setControllerFactory(type -> {
                if (type == EditProfileController.class) {
                    return new EditProfileController(context);
                }
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            Stage dialog = new Stage();
            dialog.setTitle("Edit Profile");
            dialog.initOwner((Stage) editProfileButton.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);

            dialog.showAndWait();

            loadUserProfileData();

        } catch (IOException e) {
            log.severe(e.getMessage());
        }
    }

    @FXML
    private void handleShowFollowers() {
        openUserList("Followers", true);
    }

    @FXML
    private void handleShowFollowing() {
        openUserList("Following", false);
    }

    private void openUserList(String title, boolean isFollowersList) {
        if (context == null || currentUsername == null) return;

        if (context.navigation() != null) {

//            context.navigation().showUserList(title, currentUsername, isFollowersList);
        }
    }

    private void showEmptyState(String text) {
        userTweetsContainer.getChildren().clear();
        Label label = new Label(text);
        label.setStyle("-fx-text-fill:#666666; -fx-padding:16px;");
        userTweetsContainer.getChildren().add(label);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}