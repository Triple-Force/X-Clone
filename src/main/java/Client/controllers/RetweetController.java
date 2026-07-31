package Client.controllers;

import Client.ClientApplicationContext;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import logic_core.app.dto.response.TweetResponse;
import logic_core.app.dto.timeline.TimelineTweet;
import java.util.UUID;

public class RetweetController {

    @FXML
    private TextArea quoteTextArea;

    @FXML
    private Label originalAuthorLabel;

    @FXML
    private Label originalTextLabel;

    private ClientApplicationContext context;
    private UUID targetTweetId;


    public void setDialogData(ClientApplicationContext context, TimelineTweet originalTweet) {
        this.context = context;
        if (originalTweet != null) {
            this.targetTweetId = originalTweet.tweetId();
            originalAuthorLabel.setText("@" + nullSafe(originalTweet.username()));
            originalTextLabel.setText(nullSafe(originalTweet.content()));
        }
    }

    public void setDialogData(ClientApplicationContext context, TweetResponse originalTweet) {
        this.context = context;
        if (originalTweet != null) {
            this.targetTweetId = originalTweet.id();
            originalAuthorLabel.setText("@" + (originalTweet.author() != null ? nullSafe(originalTweet.author().username()) : ""));
            originalTextLabel.setText(nullSafe(originalTweet.content()));
        }
    }

    @FXML
    private void handleRetweetSubmit() {
        if (context == null || targetTweetId == null) {
            return;
        }

        String commentText = quoteTextArea.getText();

        String contentToSend = (commentText != null && !commentText.isBlank()) ? commentText.trim() : null;

        quoteTextArea.setDisable(true);

        context.getTweetService().retweet(targetTweetId, contentToSend)
                .thenAccept(result -> Platform.runLater(() -> {
                    if (result.isSuccess()) {
                        closeStage();
                    } else {
                        quoteTextArea.setDisable(false);
                    }
                }))
                .exceptionally(error -> {
                    Platform.runLater(() -> {
                        quoteTextArea.setDisable(false);
                        error.printStackTrace();
                    });
                    return null;
                });
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) quoteTextArea.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}