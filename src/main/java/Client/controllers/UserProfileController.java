package Client.controllers;

import Client.ClientApplicationContext;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import logic_core.app.dto.response.ProfileInfoResponse;

import java.util.UUID;

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
    private UUID userId;
    private boolean isFollowing = false;
    private long followersCount = 0;

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

    public void setProfileData(ProfileInfoResponse profile) {

        this.targetUsername = profile.username();
        this.followersCount = profile.followers();
        this.userId = profile.userId();

        headerNameLabel.setText(nullSafe(profile.displayName()));
        displayNameLabel.setText(nullSafe(profile.displayName()));
        usernameLabel.setText("@" + nullSafe(profile.username()));
        bioLabel.setText(nullSafe(profile.bio()));

        tweetCountLabel.setText(profile.tweets() + " Tweets");
        followingCountLabel.setText(profile.following() + " Following");

        updateFollowersLabel();

        System.out.println(1);
        context.getFollowQueryClientService()
                .getFollowers(profile.userId())
                .thenAccept(result -> {

                    if (!result.isSuccess() || result.getData() == null)
                        return;

                    UUID currentUserId = context.getSnapshot().userId();

                    isFollowing = result.getData()
                            .followers()
                            .stream()
                            .anyMatch(u -> u.userId().equals(currentUserId));

                    Platform.runLater(this::updateFollowButtonState);
                });

        System.out.println(2);
        updateFollowButtonState();
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

        var future = isFollowing
                ? context.getRelationClientService().follow(userId)
                : context.getRelationClientService().unfollow(userId);

        future.thenAccept(result -> Platform.runLater(() -> {

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