package Client.controllers;

import Client.ClientApplicationContext;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import logic_core.app.dto.response.UserProfileResponse;

import java.net.URL;

public class UserProfileController {

    @FXML private Label headerNameLabel;
    @FXML private Label tweetCountLabel;
    @FXML private ImageView userAvatarImageView;
    @FXML private Button followButton;
    @FXML private Label displayNameLabel;
    @FXML private Label usernameLabel;
    @FXML private Label bioLabel;
    @FXML private Label followingCountLabel;
    @FXML private Label followersCountLabel;
    @FXML private VBox userTweetsContainer;

    private final ClientApplicationContext context;
    private String targetUsername;
    private boolean isFollowing = false;
    private int followersCount = 0;

    public UserProfileController(ClientApplicationContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        followingCountLabel.setOnMouseClicked(e -> openUserList("Following", false));
        followingCountLabel.setStyle("-fx-cursor: hand;");

        followersCountLabel.setOnMouseClicked(e -> openUserList("Followers", true));
        followersCountLabel.setStyle("-fx-cursor: hand;");
    }

    public void setProfileData(UserProfileResponse profile) {
        if (profile == null) return;

        this.targetUsername = profile.username();
        this.isFollowing = profile.isFollowing();
        this.followersCount = profile.followersCount();

        headerNameLabel.setText(nullSafe(profile.displayName()));
        displayNameLabel.setText(nullSafe(profile.displayName()));
        usernameLabel.setText("@" + nullSafe(profile.username()));
        bioLabel.setText(nullSafe(profile.bio()));

        tweetCountLabel.setText(profile.tweetsCount() + " Tweets");
        followingCountLabel.setText(profile.followingCount() + " Following");

        updateFollowersLabel();
        updateFollowButtonState();

        checkIfSelfProfile();
    }

    @FXML
    public void handleFollowToggle(ActionEvent event) {
        if (targetUsername == null || context == null) return;

        followButton.setDisable(true);

        if (isFollowing) {
            isFollowing = false;
            followersCount = Math.max(0, followersCount - 1);
        } else {
            isFollowing = true;
            followersCount++;
        }
        updateFollowersLabel();
        updateFollowButtonState();

        var serviceCall = isFollowing
                ? context.getRelationClientService().followUser(targetUsername)
                : context.getRelationClientService().unfollowUser(targetUsername);

        serviceCall.thenAccept(result -> Platform.runLater(() -> {
            followButton.setDisable(false);
            if (!result.isSuccess()) {
                revertFollowState();
            }
        })).exceptionally(error -> {
            Platform.runLater(() -> {
                followButton.setDisable(false);
                revertFollowState();
            });
            return null;
        });
    }

    private void revertFollowState() {
        if (isFollowing) {
            isFollowing = false;
            followersCount = Math.max(0, followersCount - 1);
        } else {
            isFollowing = true;
            followersCount++;
        }
        updateFollowersLabel();
        updateFollowButtonState();
    }

    private void updateFollowersLabel() {
        followersCountLabel.setText(followersCount + " Followers");
    }

    private void updateFollowButtonState() {
        if (isFollowing) {
            followButton.setText("Following");
            followButton.setStyle("-fx-background-color: transparent; -fx-border-color: #cfd9de; -fx-border-radius: 20; -fx-text-fill: #0f1419; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-cursor: hand;");
        } else {
            followButton.setText("Follow");
            followButton.setStyle("-fx-background-color: #0f1419; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-cursor: hand;");
        }
    }

    private void checkIfSelfProfile() {
        try {
            String currentUsername = context.getSnapshot().username();
            if (currentUsername != null && currentUsername.equals(targetUsername)) {
                followButton.setVisible(false);
                followButton.setManaged(false);
            }
        } catch (Exception ignored) {}
    }

    private void openUserList(String title, boolean isFollowers) {
        if (context == null || context.navigation() == null) return;
    }

    private String nullSafe(String val) {
        return val == null ? "" : val;
    }
}