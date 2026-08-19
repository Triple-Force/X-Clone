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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logic_core.app.dto.request.GetTimelineResponse;
import logic_core.app.dto.response.ProfileInfoResponse;
import logic_core.app.dto.timeline.TimelineTweet;
import logic_core.domain.repository.TimelineType;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Logger;

public class ProfileController {

    private static final Logger log = Logger.getLogger(ProfileController.class.getName());

    @FXML
    private Label followersCountLabel;

    @FXML
    private Label followingCountLabel;

    @FXML
    private Label headerNameLabel;

    @FXML
    private ImageView profileAvatar;

    @FXML
    private Button editProfileButton;

    @FXML
    private Label displayNameLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label bioLabel;

    @FXML
    private VBox userTweetsContainer;

    private final ClientApplicationContext context;
    private UUID profileUserId;

    private static final String DEFAULT_AVATAR_RESOURCE = "/Client/images/user (1).png";

    public ProfileController(ClientApplicationContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        if (!context.session().isLoggedIn()) {
            return;
        }

        this.profileUserId = context.session().getCurrentUserId();

        loadUserProfileData();
        loadUserTweets();
    }

    private void loadUserProfileData() {
        context.getUserClientService()
                .getProfile(profileUserId)
                .thenAccept(result -> {
                    Platform.runLater(() -> {
                        if (result == null || result.isFailure()) {
                            log.warning("Profile loading failed : " + (result == null ? "null" : result.getError()));
                            return;
                        }

                        ProfileInfoResponse profile = result.getData();
                        if (profile == null) return;

                        headerNameLabel.setText(safe(profile.displayName()));
                        displayNameLabel.setText(safe(profile.displayName()));
                        usernameLabel.setText(profile.username() == null ? "" : "@" + profile.username());
                        bioLabel.setText(safe(profile.bio()));
                        followersCountLabel.setText(String.valueOf(profile.followers()));
                        followingCountLabel.setText(String.valueOf(profile.following()));

                        loadAvatar(profile);
                    });
                })
                .exceptionally(error -> {
                    log.severe("Profile exception : " + error.getMessage());
                    return null;
                });
    }

    private void loadAvatar(ProfileInfoResponse profile) {
        if (profileAvatar == null) return;

        String avatarUrl = profile.avatarUrl();
        log.info("Loading Avatar for profile. avatarUrl = " + avatarUrl);

        if (avatarUrl != null && !avatarUrl.isBlank()) {
            try {
                String cleanPath = avatarUrl.startsWith("/") || avatarUrl.startsWith("\\")
                        ? avatarUrl.substring(1)
                        : avatarUrl;

                File avatarFile = new File("data", cleanPath);

                if (!avatarFile.exists()) {
                    String userDir = System.getProperty("user.dir");
                    avatarFile = new File(userDir + File.separator + "data", cleanPath);
                }

                log.info("Resolved Avatar Absolute Path: " + avatarFile.getAbsolutePath() + " | Exists: " + avatarFile.exists());

                if (avatarFile.exists()) {
                    Image image = new Image(avatarFile.toURI().toString(), true);
                    profileAvatar.setImage(image);
                    return;
                } else {
                    log.warning("Avatar file NOT found on disk: " + avatarFile.getAbsolutePath());
                }
            } catch (Exception e) {
                log.warning("Failed to load user avatar: " + e.getMessage());
            }
        }
        setDefaultAvatar();
    }

    private void setDefaultAvatar() {
        try {
            URL resource = getClass().getResource(DEFAULT_AVATAR_RESOURCE);
            if (resource != null) {
                profileAvatar.setImage(new Image(resource.toExternalForm(), true));
            } else {
                profileAvatar.setImage(null);
                log.warning("Default avatar resource not found at: " + DEFAULT_AVATAR_RESOURCE);
            }
        } catch (Exception e) {
            profileAvatar.setImage(null);
            log.warning("Failed to load default avatar: " + e.getMessage());
        }
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
                .thenAccept(result -> {
                    Platform.runLater(() -> {
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
                    });
                })
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

            controller.setOnDeleteSuccess(() -> {
                userTweetsContainer.getChildren().remove(card);
                if (userTweetsContainer.getChildren().isEmpty()) {
                    showEmptyState("No tweets yet");
                }
            });

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
    private void handleShowFollowers() {}

    @FXML
    private void handleShowFollowing() {}

    private void showEmptyState(String text) {
        userTweetsContainer.getChildren().clear();
        Label label = new Label(text);
        label.setStyle("-fx-text-fill:#666666;" + "-fx-padding:16px;");
        userTweetsContainer.getChildren().add(label);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}