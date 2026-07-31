package Client.controllers;

import Client.ClientApplicationContext;
import logic_core.app.dto.response.TweetResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommentController {

    private static final Logger log = Logger.getLogger(CommentController.class.getName());

    @FXML
    private Label targetDisplayNameLabel;

    @FXML
    private Label targetUsernameLabel;

    @FXML
    private Label targetTweetTextLabel;

    @FXML
    private TextArea commentTextArea;

    private ClientApplicationContext context;
    private TweetResponse targetTweet;

    public void setDialogData(ClientApplicationContext context, TweetResponse targetTweet) {
        this.context = context;
        this.targetTweet = targetTweet;

        if (targetTweet != null) {
            targetTweetTextLabel.setText(targetTweet.content() != null ? targetTweet.content() : "");

            if (targetTweet.author() != null) {
                String username = targetTweet.author().username();
                String displayName = targetTweet.author().displayName();

                targetDisplayNameLabel.setText((displayName != null && !displayName.isBlank()) ? displayName : username);
                targetUsernameLabel.setText("@" + (username != null ? username : "unknown"));
            } else {
                targetDisplayNameLabel.setText("User");
                targetUsernameLabel.setText("@unknown");
            }
        }
    }

    @FXML
    private void handleSubmitComment(ActionEvent event) {
        String commentContent = commentTextArea.getText();

        if (commentContent == null || commentContent.trim().isEmpty()) {
            return;
        }

        if (targetTweet == null || context == null) {
            log.warning("Cannot submit comment: targetTweet or context is null.");
            return;
        }

        setInputsDisabled(true);

        context.getTweetService().replyTweet(targetTweet.id(), commentContent.trim(), Collections.emptyList())
                .thenAccept(result -> Platform.runLater(() -> {
                    if (result.isSuccess()) {
                        log.info("Reply posted successfully for tweet: " + targetTweet.id());
                        closeDialog();
                    } else {
                        setInputsDisabled(false);
                        log.warning("Failed to post reply: " + result.getMessage());
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        setInputsDisabled(false);
                        log.log(Level.SEVERE, "Error posting reply", ex);
                    });
                    return null;
                });
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) commentTextArea.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void setInputsDisabled(boolean disabled) {
        commentTextArea.setDisable(disabled);
    }
}