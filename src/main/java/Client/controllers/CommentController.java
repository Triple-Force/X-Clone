package Client.controllers;

import Client.ClientApplicationContext;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import logic_core.app.dto.timeline.TimelineTweet;

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
    private TimelineTweet targetTweet;

    public void setDialogData(ClientApplicationContext context, TimelineTweet targetTweet) {
        this.context = context;
        this.targetTweet = targetTweet;

        if (targetTweet == null)
        {
            return;
        }

        targetDisplayNameLabel.setText(
                targetTweet.displayName() == null
                        ? ""
                        : targetTweet.displayName()
        );

        targetUsernameLabel.setText(
                targetTweet.username() == null
                        ? ""
                        : "@" + targetTweet.username()
        );

        targetTweetTextLabel.setText(
                targetTweet.content() == null
                        ? ""
                        : targetTweet.content()
        );

        commentTextArea.clear();
        commentTextArea.requestFocus();
    }

    @FXML
    private void handleSubmitComment(ActionEvent event)
    {
        if (context == null || targetTweet == null)
        {
            return;
        }

        String content = commentTextArea.getText();

        if (content == null || content.isBlank())
        {
            return;
        }

        setInputsDisabled(true);

        context.getTweetService()
                .replyTweet(
                        targetTweet.tweetId(),
                        content.trim(),
                        Collections.emptyList()
                )
                .thenAccept(result ->
                        Platform.runLater(() ->
                        {
                            if (result.isSuccess())
                            {
                                closeDialog();
                            }
                            else
                            {
                                setInputsDisabled(false);
                                log.warning(
                                        "Reply failed: "
                                                + result.getError()
                                );
                            }
                        }))
                .exceptionally(ex ->
                {
                    Platform.runLater(() ->
                    {
                        setInputsDisabled(false);
                        log.log(
                                Level.SEVERE,
                                "Reply failed",
                                ex
                        );
                    });

                    return null;
                });
    }

    @FXML
    private void handleCancel(ActionEvent event)
    {
        closeDialog();
    }

    private void setInputsDisabled(boolean disabled)
    {
        commentTextArea.setDisable(disabled);
    }

    private void closeDialog()
    {
        Stage stage =
                (Stage) commentTextArea.getScene().getWindow();

        if (stage != null)
        {
            stage.close();
        }
    }
}