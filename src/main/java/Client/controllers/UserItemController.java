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

public class UserItemController
{

    @FXML
    private HBox rootContainer;

    @FXML
    private ImageView avatarImageView;

    @FXML
    private Label displayNameLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Button followButton;

    private static final String DEFAULT_AVATAR =
            "/Client/images/default-avatar.png";

    private final ClientApplicationContext context;

    private UserSummaryResponse user;


    private boolean isFollowing = false;

    public UserItemController(ClientApplicationContext context)
    {
        this.context = context;
    }

    @FXML
    private void initialize()
    {
        rootContainer.setOnMouseClicked(e -> openProfile());

        followButton.setOnAction(e -> toggleFollow());

        rootContainer.setStyle("-fx-cursor: hand;");
    }

    public void setUser(UserSummaryResponse user) {
        this.user = user;

        if (user == null)
            return;

        displayNameLabel.setText(
                user.displayName() == null
                        ? ""
                        : user.displayName()
        );

        usernameLabel.setText(
                user.username() == null
                        ? ""
                        : "@" + user.username()
        );

        loadAvatar(user.avatarUrl());

        hideButtonIfCurrentUser();

        /*
            اگر بعداً following به DTO اضافه شد:

            isFollowing = user.following();
         */

        updateFollowButton();
    }

    private void toggleFollow()
    {
        if (user == null)
            return;

        followButton.setDisable(true);

        var future =
                isFollowing
                        ? context.getRelationClientService().unfollow(user.userId())
                        : context.getRelationClientService().follow(user.userId());

        future.thenAccept(result ->
                Platform.runLater(() ->
                {
                    followButton.setDisable(false);

                    if (!result.isSuccess())
                        return;

                    isFollowing = !isFollowing;

                    updateFollowButton();
                })
        ).exceptionally(ex ->
        {
            Platform.runLater(() ->
                    followButton.setDisable(false));

            ex.printStackTrace();

            return null;
        });
    }

    private void updateFollowButton()
    {
        if (isFollowing)
        {
            followButton.setText("Following");

            followButton.setStyle("""
                    -fx-background-color: transparent;
                    -fx-border-color: #cfd9de;
                    -fx-border-radius: 20;
                    -fx-text-fill: #0f1419;
                    -fx-font-weight: bold;
                    -fx-padding: 6 16;
                    -fx-cursor: hand;
                    """);
        }
        else
        {
            followButton.setText("Follow");

            followButton.setStyle("""
                    -fx-background-color: #0f1419;
                    -fx-text-fill: white;
                    -fx-background-radius: 20;
                    -fx-font-weight: bold;
                    -fx-padding: 6 16;
                    -fx-cursor: hand;
                    """);
        }
    }

    private void openProfile()
    {
        if (user == null)
            return;

        /*
            وقتی Navigation کامل شد:

            context.navigation().showProfile(user.userId());

            یا

            context.navigation().showProfile(user.username());
         */
    }

    private void hideButtonIfCurrentUser()
    {
        UUID currentUser =
                context.getSnapshot().userId();

        if (currentUser != null &&
                currentUser.equals(user.userId()))
        {
            followButton.setVisible(false);
            followButton.setManaged(false);
        }
    }

    private void loadAvatar(String avatarUrl)
    {
        if (avatarUrl == null || avatarUrl.isBlank())
        {
            loadDefaultAvatar();
            return;
        }

        try
        {
            Image image = new Image(avatarUrl, true);

            image.exceptionProperty().addListener((obs, old, ex) ->
            {
                if (ex != null)
                {
                    Platform.runLater(this::loadDefaultAvatar);
                }
            });

            avatarImageView.setImage(image);
        }
        catch (Exception e)
        {
            loadDefaultAvatar();
        }
    }

    private void loadDefaultAvatar()
    {
        URL url = getClass().getResource(DEFAULT_AVATAR);

        if (url != null)
        {
            avatarImageView.setImage(
                    new Image(url.toExternalForm())
            );
        }
    }
}