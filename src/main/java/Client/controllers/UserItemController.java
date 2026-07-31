package Client.controllers;

import Client.ClientApplicationContext;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import logic_core.app.dto.response.UserSummaryResponse;

import java.net.URL;
import java.util.UUID;

public class UserItemController {

    @FXML private HBox rootContainer;
    @FXML private ImageView avatarImageView;
    @FXML private Label displayNameLabel;
    @FXML private Label usernameLabel;
    @FXML private Button followButton;

    private final ClientApplicationContext context;
    private static final String DEFAULT_AVATAR_RESOURCE = "/Client/images/default-avatar.png";
    private UserSummaryResponse user;
    private boolean isFollowing;

    public UserItemController(ClientApplicationContext context) {
        this.context = context;
    }

    @FXML
    private void initialize() {
        if (followButton != null) {
            followButton.setOnAction(e -> handleFollowToggle());
        }

        if (rootContainer != null) {
            rootContainer.setOnMouseClicked(e -> handleOpenProfile());
            rootContainer.setStyle("-fx-cursor: hand;");
        }
    }

    public void setUser(UserSummaryResponse user) {
        this.user = user;
        if (user == null) return;

        displayNameLabel.setText(nullSafe(user.displayName()));
        usernameLabel.setText(user.username() == null ? "" : "@" + user.username());

        setAvatar(user.avatarUrl());

        context.getFollowQueryClientService()
                .getFollowers(user.userId())
                .thenAccept(result -> {
                    if (result.isSuccess()) {
                        UUID currentUserId = context.getSnapshot().userId();

                        isFollowing = result.getData()
                                .followers()
                                .stream()
                                .anyMatch(follower -> follower.userId().equals(currentUserId));

                        Platform.runLater(this::updateFollowButtonState);
                    }
                });

        updateFollowButtonState();

        checkSelfUser();
    }

    private void updateFollowButtonState() {
        if (followButton == null) return;

        if (isFollowing) {
            followButton.setText("Following");
            followButton.setStyle("-fx-background-color: transparent; -fx-border-color: #cfd9de; -fx-border-radius: 20; -fx-text-fill: #0f1419; -fx-font-weight: bold; -fx-padding: 6 16; -fx-cursor: hand;");
        } else {
            followButton.setText("Follow");
            followButton.setStyle("-fx-background-color: #0f1419; -fx-background-radius: 20; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 16; -fx-cursor: hand;");
        }
    }

    private void handleFollowToggle() {
        if (user == null || context == null) return;

        followButton.setDisable(true);

        var serviceCall = isFollowing
                ? context.getRelationClientService().unfollow(user.userId())
                : context.getRelationClientService().follow(user.userId());

        serviceCall.thenAccept(result -> Platform.runLater(() -> {
            followButton.setDisable(false);
            if (result.isSuccess()) {
                isFollowing = !isFollowing;
                updateFollowButtonState();
            }
        })).exceptionally(error -> {
            Platform.runLater(() -> followButton.setDisable(false));
            error.printStackTrace();
            return null;
        });
    }

    private void handleOpenProfile() {
        if (user == null || context == null || context.navigation() == null) return;

//        context.navigation().navigateToProfile(user.username());
    }

    private void checkSelfUser() {
        if (context == null || user == null || followButton == null) return;
        try {
            UUID currentUserId = context.getSnapshot().userId();
            if (currentUserId != null && currentUserId.equals(user.userId())) {
                followButton.setVisible(false);
                followButton.setManaged(false);
            }
        } catch (Exception ignored) {}
    }

    private void setAvatar(String avatarUrl) {
        try {
            if (avatarUrl != null && !avatarUrl.isBlank()) {
                avatarImageView.setImage(new Image(avatarUrl, true));
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
                avatarImageView.setImage(new Image(resource.toExternalForm(), true));
            }
        } catch (Exception ignored) {}
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}