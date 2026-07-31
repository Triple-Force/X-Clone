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
import logic_core.app.dto.response.TweetResponse;
import logic_core.app.dto.response.UserSummaryResponse;
import logic_core.app.dto.timeline.TimelineTweet;

import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class TweetItemController {

    @FXML
    public VBox pollContainer;

    @FXML
    public ImageView mediaImageView;

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

    @FXML
    private VBox repliesContainer;

    private final ClientApplicationContext context;
    private static final String DEFAULT_AVATAR_RESOURCE = "/Client/images/default-avatar.png";
    private TimelineTweet tweet;

    public TweetItemController(ClientApplicationContext context) {
        this.context = context;
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

        if (tweetTextLabel != null) {
            tweetTextLabel.setOnMouseClicked(e -> toggleReplies());
            tweetTextLabel.setStyle("-fx-cursor: hand;");
        }
    }

    public void setTweet(TimelineTweet tweet) {
        this.tweet = tweet;
        this.currentLikeCount = tweet.likeCount();
        if (tweet == null) {
            clear();
            return;
        }

        displayNameLabel.setText(nullSafe(tweet.displayName()));
        usernameLabel.setText(tweet.username() == null ? "" : "@" + tweet.username());
        dateLabel.setText(formatDate(tweet.publishedAt()));
        tweetTextLabel.setText(nullSafe(tweet.content()));

        commentButton.setText("💬 " + tweet.replyCount());
        retweetButton.setText("🔁 " + tweet.retweetCount());
        likeButton.setText("❤ " + tweet.likeCount());

        setAvatar(tweet.avatarUrl());

        if (tweet.mediaUrls() != null && !tweet.mediaUrls().isEmpty()) {
            String firstMediaUrl = tweet.mediaUrls().get(0);
            if (firstMediaUrl != null && !firstMediaUrl.isBlank()) {
                setTweetMedia(firstMediaUrl);
            } else {
                hideMedia();
            }
        } else {
            hideMedia();
        }

        checkAndDeleteVisibility(tweet);
    }

    private void checkAndDeleteVisibility(TimelineTweet tweet) {
        if (deleteButton == null || context == null) {
            return;
        }

        try {
            String currentUsername = context.getCurrentUsername();
            if (currentUsername != null && currentUsername.equals(tweet.username())) {
                deleteButton.setVisible(true);
                deleteButton.setManaged(true);
            } else {
                deleteButton.setVisible(false);
                deleteButton.setManaged(false);
            }
        } catch (Exception e) {
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
        }
    }

    private void clear() {
        displayNameLabel.setText("");
        usernameLabel.setText("");
        dateLabel.setText("");
        tweetTextLabel.setText("");
        commentButton.setText("💬 0");
        retweetButton.setText("🔁 0");
        likeButton.setText("❤ 0");

        hideMedia();

        if (deleteButton != null) {
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
        }

        setDefaultAvatar();

        if (repliesContainer != null) {
            repliesContainer.getChildren().clear();
            repliesContainer.setVisible(false);
            repliesContainer.setManaged(false);
        }
    }

    private void setTweetMedia(String mediaUrl) {
        if (mediaImageView == null) return;
        try {
            mediaImageView.setImage(new Image(mediaUrl, true));
            mediaImageView.setVisible(true);
            mediaImageView.setManaged(true);
        } catch (Exception e) {
            hideMedia();
        }
    }

    private void hideMedia() {
        if (mediaImageView != null) {
            mediaImageView.setImage(null);
            mediaImageView.setVisible(false);
            mediaImageView.setManaged(false);
        }
    }

    public void toggleReplies() {
        if (tweet == null || context == null || repliesContainer == null) {
            return;
        }

        if (!repliesContainer.isVisible()) {
            context.getTweetService().getReplies(tweet.tweetId())
                    .thenAccept(result -> Platform.runLater(() -> {
                        if (result.isSuccess() && result.getData() != null) {
                            repliesContainer.getChildren().clear();

                            for (TimelineTweet reply : result.getData()) {
                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client/fxml/TweetItem.fxml"));
                                    loader.setControllerFactory(param -> new TweetItemController(context));
                                    VBox replyNode = loader.load();

                                    TweetItemController controller = loader.getController();
                                    controller.setTweet(reply);

                                    repliesContainer.getChildren().add(replyNode);
                                } catch (Exception e) {
                                    System.err.println("Failed to load reply item: " + e.getMessage());
                                }
                            }

                            repliesContainer.setVisible(true);
                            repliesContainer.setManaged(true);
                        }
                    }))
                    .exceptionally(error -> {
                        error.printStackTrace();
                        return null;
                    });
        } else {
            repliesContainer.setVisible(false);
            repliesContainer.setManaged(false);
        }
    }

    private void handleLike() {
        if (tweet == null || context == null) {
            return;
        }

        likeButton.setDisable(true);

        context.getTweetService().likeTweet(tweet.tweetId())
                .thenAccept(result -> Platform.runLater(() -> {
                    likeButton.setDisable(false);

                    if (result.isFailure()) {
                        return;
                    }

                    long newCount = result.getData().liked() ? tweet.likeCount() + 1 : Math.max(0, tweet.likeCount() - 1);
                    likeButton.setText("❤ " + newCount);
                }))
                .exceptionally(error -> {
                    Platform.runLater(() -> likeButton.setDisable(false));
                    return null;
                });
    }

    private void handleRetweet() {
        if (tweet == null || context == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client/fxml/QuoteTweetDialog.fxml"));

            loader.setControllerFactory(param -> {
                if (param == RetweetController.class) {
                    return new RetweetController();
                }
                try {
                    return param.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            RetweetController controller = loader.getController();

            controller.setDialogData(this.context, tweet);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Quote Tweet");
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            Stage mainStage = (Stage) retweetButton.getScene().getWindow();
            dialogStage.initOwner(mainStage);

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            dialogStage.setOnShown(e -> {
                dialogStage.setX(mainStage.getX() + (mainStage.getWidth() - dialogStage.getWidth()) / 2);
                dialogStage.setY(mainStage.getY() + (mainStage.getHeight() - dialogStage.getHeight()) / 2);
            });

            dialogStage.showAndWait();

        } catch (Exception e) {
            System.err.println("Failed to open quote tweet dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDelete() {
        if (tweet == null || context == null) {
            return;
        }

        deleteButton.setDisable(true);

        context.getTweetService().deleteTweet(tweet.tweetId())
                .thenAccept(result -> Platform.runLater(() -> {
                    if (result.isSuccess()) {
                        if (deleteButton.getScene() != null) {
                            javafx.scene.Node currentRoot = deleteButton;
                            while (currentRoot.getParent() != null) {
                                currentRoot = currentRoot.getParent();
                                if (currentRoot instanceof VBox && currentRoot.getParent() instanceof VBox && ((VBox)currentRoot.getParent()).getChildren().contains(currentRoot)) {
                                    break;
                                }
                            }
                            if (currentRoot.getParent() instanceof VBox) {
                                ((VBox) currentRoot.getParent()).getChildren().remove(currentRoot);
                            }
                        }
                    } else {
                        deleteButton.setDisable(false);
                    }
                }))
                .exceptionally(error -> {
                    Platform.runLater(() -> deleteButton.setDisable(false));
                    error.printStackTrace();
                    return null;
                });
    }

    private void handleReply() {
        if (tweet == null || context == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client/fxml/CommentDialog.fxml"));

            loader.setControllerFactory(param -> {
                if (param == CommentController.class) {
                    return new CommentController();
                }
                try {
                    return param.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            CommentController controller = loader.getController();

            UserSummaryResponse authorSummary = new UserSummaryResponse(
                    null,
                    tweet.username(),
                    tweet.displayName(),
                    tweet.avatarUrl(),
                    false
            );

            TweetResponse targetTweetResponse = new TweetResponse(
                    tweet.tweetId(),
                    tweet.content(),
                    authorSummary,
                    false,
                    false,
                    tweet.publishedAt(),
                    tweet.publishedAt(),
                    null,
                    null,
                    null,
                    tweet.likeCount(),
                    tweet.replyCount(),
                    tweet.retweetCount()
            );

            controller.setDialogData(this.context, targetTweetResponse);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Reply to Tweet");
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            Stage mainStage = (Stage) commentButton.getScene().getWindow();
            dialogStage.initOwner(mainStage);

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            dialogStage.setOnShown(e -> {
                dialogStage.setX(mainStage.getX() + (mainStage.getWidth() - dialogStage.getWidth()) / 2);
                dialogStage.setY(mainStage.getY() + (mainStage.getHeight() - dialogStage.getHeight()) / 2);
            });

            dialogStage.showAndWait();

        } catch (Exception e) {
            System.err.println("Failed to open comment dialog: " + e.getMessage());
            e.printStackTrace();
        }
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
}