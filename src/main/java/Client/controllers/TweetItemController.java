package Client.controllers;

import Client.ClientApplicationContext;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import logic_core.app.dto.timeline.TimelineTweet;

import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

public class TweetItemController {

    private static final Logger log = Logger.getLogger(TweetItemController.class.getName());
    private static final String DEFAULT_AVATAR_RESOURCE = "/Client/images/user (1).png";

    @FXML
    private ImageView avatarImageView;

    @FXML
    private Label displayNameLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label tweetTextLabel;

    @FXML
    private Button commentButton;

    @FXML
    private Button retweetButton;

    @FXML
    private Button likeButton;

    @FXML
    private Button deleteButton;

    private final ClientApplicationContext context;
    private TimelineTweet tweet;
    private boolean liked = false;
    private long currentLikeCount = 0;
    private Runnable onDeleteSuccess;

    public TweetItemController(ClientApplicationContext context) {
        this.context = context;
    }

    @FXML
    private void initialize() {
        if (likeButton != null) {
            likeButton.setOnAction(this::handleLike);
        }
        if (deleteButton != null) {
            deleteButton.setOnAction(this::handleDelete);
        }
    }

    public void setOnDeleteSuccess(Runnable onDeleteSuccess) {
        this.onDeleteSuccess = onDeleteSuccess;
    }

    public void setTweet(TimelineTweet tweet) {
        this.tweet = tweet;
        if (tweet == null) {
            clear();
            return;
        }

        this.currentLikeCount = tweet.likeCount();
        this.liked = tweet.isLiked();

        displayNameLabel.setText(nullSafe(tweet.displayName()));
        usernameLabel.setText(tweet.username() == null ? "" : "@" + tweet.username());
        dateLabel.setText(formatDate(tweet.publishedAt()));
        tweetTextLabel.setText(nullSafe(tweet.content()));

        commentButton.setText("💬 " + tweet.replyCount());
        retweetButton.setText("🔁 " + tweet.retweetCount());

        updateLikeButtonUI();
        setAvatar(tweet.avatarUrl());
        checkDeletePermission();
    }

    @FXML
    private void handleLike(ActionEvent event) {
        if (tweet == null || (likeButton != null && likeButton.isDisabled())) return;

        if (likeButton != null) {
            likeButton.setDisable(true);
        }

        final boolean currentlyLiked = this.liked;

        var serviceCall = currentlyLiked
                ? context.getTweetService().unlikeTweet(tweet.tweetId())
                : context.getTweetService().likeTweet(tweet.tweetId());

        serviceCall.thenAccept(result -> Platform.runLater(() -> {
            if (likeButton != null) {
                likeButton.setDisable(false);
            }

            if (result == null || result.isFailure()) {
                log.warning("Like/Unlike action failed: " + (result == null ? "null" : result.getError()));
                return;
            }

            this.liked = !currentlyLiked;
            if (this.liked) {
                this.currentLikeCount++;
            } else {
                this.currentLikeCount = Math.max(0, this.currentLikeCount - 1);
            }

            updateLikeButtonUI();
        })).exceptionally(error -> {
            Platform.runLater(() -> {
                if (likeButton != null) {
                    likeButton.setDisable(false);
                }
            });
            log.severe("Error in like/unlike action: " + error.getMessage());
            return null;
        });
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (tweet == null || (deleteButton != null && deleteButton.isDisabled())) return;

        if (deleteButton != null) {
            deleteButton.setDisable(true);
        }

        context.getTweetService().deleteTweet(tweet.tweetId())
                .thenAccept(result -> Platform.runLater(() -> {
                    if (deleteButton != null) {
                        deleteButton.setDisable(false);
                    }

                    if (result == null || result.isFailure()) {
                        log.warning("Delete tweet failed: " + (result == null ? "null" : result.getError()));
                        return;
                    }

                    if (onDeleteSuccess != null) {
                        onDeleteSuccess.run();
                    }
                }))
                .exceptionally(error -> {
                    Platform.runLater(() -> {
                        if (deleteButton != null) {
                            deleteButton.setDisable(false);
                        }
                    });
                    log.severe("Error deleting tweet: " + error.getMessage());
                    return null;
                });
    }

    private void updateLikeButtonUI() {
        if (likeButton == null) return;

        likeButton.setText(" " + currentLikeCount);

        javafx.scene.control.Label iconLabel = new javafx.scene.control.Label(liked ? "♥" : "♡");

        String color = liked ? "#e0245e" : "#666666";
        iconLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + color + ";");

        likeButton.setGraphic(iconLabel);

        likeButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: transparent; " +
                        "-fx-padding: 2 6; " +
                        "-fx-font-size: 13px; " +
                        "-fx-text-fill: " + color + "; " +
                        "-fx-cursor: hand;"
        );
    }

    private void checkDeletePermission() {
        if (deleteButton == null) return;

        boolean isOwner = context.session().isLoggedIn() &&
                context.session().getCurrentUserId().equals(tweet.authorId());

        deleteButton.setVisible(isOwner);
        deleteButton.setManaged(isOwner);
    }

    private void setAvatar(String avatarUrl) {
        if (avatarImageView == null) return;

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

                if (avatarFile.exists()) {
                    avatarImageView.setImage(new Image(avatarFile.toURI().toString(), true));
                    return;
                }
            } catch (Exception e) {
                log.warning("Failed to load tweet avatar: " + e.getMessage());
            }
        }
        setDefaultAvatar();
    }

    private void setDefaultAvatar() {
        try {
            URL resource = getClass().getResource(DEFAULT_AVATAR_RESOURCE);
            if (resource != null) {
                avatarImageView.setImage(new Image(resource.toExternalForm(), true));
            } else {
                avatarImageView.setImage(null);
            }
        } catch (Exception e) {
            avatarImageView.setImage(null);
        }
    }

    private void clear() {
        displayNameLabel.setText("");
        usernameLabel.setText("");
        dateLabel.setText("");
        tweetTextLabel.setText("");
        commentButton.setText("💬 0");
        retweetButton.setText("🔁 0");
        if (likeButton != null) {
            likeButton.setText("🖤 0");
        }
        if (deleteButton != null) {
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
        }
    }

    private String nullSafe(String text) {
        return text == null ? "" : text;
    }

    private String formatDate(Object dateObj) {
        return dateObj == null ? "" : dateObj.toString();
    }
}