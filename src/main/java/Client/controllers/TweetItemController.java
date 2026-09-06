package Client.controllers;

import Client.ClientApplicationContext;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logic_core.app.dto.response.LikeResponse;
import logic_core.app.dto.timeline.TimelineTweet;

import java.io.File;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

public class TweetItemController {

    private static final Logger log = Logger.getLogger(TweetItemController.class.getName());

    @FXML public VBox pollContainer;
    @FXML public ImageView mediaImageView;
    @FXML private ImageView avatarImageView;
    @FXML private Label displayNameLabel;
    @FXML private Label usernameLabel;
    @FXML private Label dateLabel;
    @FXML private Label tweetTextLabel;
    @FXML private Button commentButton;
    @FXML private Button retweetButton;
    @FXML private Button likeButton;
    @FXML private Button deleteButton;

    private final ClientApplicationContext context;
    private long currentLikeCount;
    private boolean liked;
    private TimelineTweet tweet;

    private Runnable onDeleteSuccess;

    private static final String DEFAULT_AVATAR_RESOURCE = "/Client/images/user (1).png";

    public TweetItemController(ClientApplicationContext context) {
        this.context = context;
    }

    public void setOnDeleteSuccess(Runnable onDeleteSuccess) {
        this.onDeleteSuccess = onDeleteSuccess;
    }

    @FXML
    private void initialize() {
        clear();

        likeButton.setOnAction(e -> handleLike());
        retweetButton.setOnAction(e -> handleRetweet());
        commentButton.setOnAction(e -> handleReply());

        if (deleteButton != null) {
            deleteButton.setOnAction(e -> handleDelete());
        }
    }

    public void setTweet(TimelineTweet tweet) {
        this.tweet = tweet;
        if (tweet == null) {
            clear();
            return;
        }

        this.currentLikeCount = tweet.likeCount();

        displayNameLabel.setText(nullSafe(tweet.displayName()));
        usernameLabel.setText(tweet.username() == null ? "" : "@" + tweet.username());
        dateLabel.setText(formatDate(tweet.publishedAt()));
        tweetTextLabel.setText(nullSafe(tweet.content()));

        commentButton.setText("💬 " + tweet.replyCount());
        retweetButton.setText("🔁 " + tweet.retweetCount());
        updateLikeButton();
        setAvatar(tweet.avatarUrl());
        checkDeletePermission();

        loadLikeState();
    }


    private void loadLikeState() {
        likeButton.setDisable(true);
        if (tweet == null)
            return;

        likeButton.setDisable(true);

        context.getUserClientService()
                .isLike(tweet.tweetId())
                .thenAccept(result -> Platform.runLater(() -> {

                    likeButton.setDisable(false);

                    if (result.isFailure())
                        return;

                    liked = result.getData().liked();
                    updateLikeButton();

                }))
                .exceptionally(error -> {

                    Platform.runLater(() -> {
                        likeButton.setDisable(false);
                    });

                    return null;
                });
    }

    private void checkDeletePermission() {
        if (deleteButton == null || tweet == null) return;

        UUID currentUserId = context.session().getCurrentUserId();
        boolean isOwner = currentUserId != null && currentUserId.equals(tweet.authorId());

        deleteButton.setVisible(isOwner);
        deleteButton.setManaged(isOwner);
    }

    private void clear() {
        displayNameLabel.setText("");
        usernameLabel.setText("");
        dateLabel.setText("");
        tweetTextLabel.setText("");
        commentButton.setText("💬 0");
        retweetButton.setText("🔁 0");

        if (deleteButton != null) {
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
        }

        liked = false;
        currentLikeCount = 0;
        updateLikeButton();
        setDefaultAvatar();
    }

    private void setAvatar(String avatarUrl) {
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
            }
        } catch (Exception ignored) {
        }
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String formatDate(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        try {
            return dateTime.format(
                    DateTimeFormatter.ofLocalizedDateTime(
                            FormatStyle.MEDIUM,
                            FormatStyle.SHORT
                    ).withLocale(Locale.getDefault())
            );
        } catch (Exception e) {
            return dateTime.toString();
        }
    }

    private void handleLike() {

        if (tweet == null)
            return;

        likeButton.setDisable(true);

        boolean oldLiked = liked;
        long oldLikeCount = currentLikeCount;

        context.getTweetService()
                .likeTweet(tweet.tweetId())
                .thenAccept(result -> Platform.runLater(() -> {

                    if (result.isFailure()) {

                        likeButton.setDisable(false);

                        liked = oldLiked;
                        currentLikeCount = oldLikeCount;
                        updateLikeButton();
                        return;
                    }


                    LikeResponse response = result.getData();
                    if (response != null) {
                        liked = response.liked();
                        currentLikeCount = response.totalLikesCount();
                    }
                    likeButton.setDisable(false);
                    updateLikeButton();

                }))
                .exceptionally(error -> {

                    Platform.runLater(() -> {

                        likeButton.setDisable(false);

                        liked = oldLiked;
                        currentLikeCount = oldLikeCount;

                        updateLikeButton();
                    });

                    return null;
                });
    }

    private void updateLikeButton() {

        likeButton.setText("❤ " + currentLikeCount);

    }

    private void handleRetweet() {
        if (tweet == null) return;

        retweetButton.setDisable(true);

        context.getTweetService().retweet(tweet.tweetId(), null)
                .thenAccept(result -> Platform.runLater(() -> {
                    retweetButton.setDisable(false);

                    if (result.isFailure()) return;

                    long newCount = tweet.retweetCount() + 1;
                    retweetButton.setText("🔁 " + newCount);
                }))
                .exceptionally(error -> {
                    Platform.runLater(() -> retweetButton.setDisable(false));
                    return null;
                });
    }

    private void handleDelete() {
        if (tweet == null) return;

        deleteButton.setDisable(true);

        context.getTweetService().deleteTweet(tweet.tweetId())
                .thenAccept(result -> Platform.runLater(() -> {
                    deleteButton.setDisable(false);

                    if (result.isFailure()) {
                        log.warning("Failed to delete tweet: " + result.getError());
                        return;
                    }

                    log.info("Tweet deleted successfully on server!");

                    if (onDeleteSuccess != null) {
                        onDeleteSuccess.run();
                    }
                }))
                .exceptionally(error -> {
                    Platform.runLater(() -> deleteButton.setDisable(false));
                    return null;
                });
    }

    private void handleReply() {
        if (tweet == null) {
            return;
        }

        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/Client/fxml/Comment.fxml"));

            Parent root = loader.load();

            CommentController controller = loader.getController();
            controller.setDialogData(context, tweet);

            Stage stage = new Stage();
            stage.setTitle("Reply");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (Exception e) {
            log.severe(e.getMessage());
        }
    }
}