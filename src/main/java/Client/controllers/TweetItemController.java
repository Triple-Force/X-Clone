package Client.controllers;

import Client.ClientApplicationContext;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
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

    private final ClientApplicationContext context;
    private long currentLikeCount;
    private boolean liked;

    public TweetItemController (ClientApplicationContext context)
    {
        this.context = context;
    }

    private static final String DEFAULT_AVATAR_RESOURCE = "/Client/images/default-avatar.png";

    private TimelineTweet tweet;
    @FXML
    private void initialize() {
        clear();

        likeButton.setOnAction(e -> handleLike());

        retweetButton.setOnAction(e -> handleRetweet());

        commentButton.setOnAction(e -> handleReply());
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
    }

    private void clear() {
        displayNameLabel.setText("");
        usernameLabel.setText("");
        dateLabel.setText("");
        tweetTextLabel.setText("");
        commentButton.setText("💬 0");
        retweetButton.setText("🔁 0");
        likeButton.setText("❤ 0");
        setDefaultAvatar();
    }

    private void setAvatar(String avatarUrl) {
        try {
            Image image = null;

            if (avatarUrl != null && !avatarUrl.isBlank()) {
                image = new Image(avatarUrl, true);
            } else {
                setDefaultAvatar();
                return;
            }

            avatarImageView.setImage(image);
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

    private void handleLike()
    {
        if (tweet == null)
        {
            return;
        }

        likeButton.setDisable(true);

        context.getTweetService().likeTweet(tweet.tweetId())
                .thenAccept(result -> Platform.runLater(() ->
                {
                    likeButton.setDisable(false);

                    if (result.isFailure())
                    {
                        return;
                    }

                    if (result.getData().liked()) {
                        currentLikeCount++;
                    } else if (!result.getData().liked() && liked){
                        currentLikeCount--;
                    }

                    likeButton.setText("❤ " + currentLikeCount);
                    liked = result.getData().liked();

                }))
                .exceptionally(error ->
                {
                    Platform.runLater(() ->
                            likeButton.setDisable(false));

                    return null;
                });
    }


    private void handleRetweet()
    {
        if (tweet == null)
        {
            return;
        }

        retweetButton.setDisable(true);

        context.getTweetService().retweet(tweet.tweetId(), null)
                .thenAccept(result -> Platform.runLater(() ->
                {
                    retweetButton.setDisable(false);

                    if (result.isFailure())
                    {
                        return;
                    }

                    long newCount = tweet.retweetCount() + 1;

                    retweetButton.setText("🔁 " + newCount);
                }))
                .exceptionally(error ->
                {
                    Platform.runLater(() ->
                            retweetButton.setDisable(false));

                    return null;
                });
    }



    private void handleReply()
    {
        if (tweet == null)
        {
            return;
        }

        System.out.println("Reply to tweet: " + tweet.tweetId());

        // TODO
        // Navigation به ReplyScreen
        // یا باز کردن Dialog نوشتن Reply
    }
}