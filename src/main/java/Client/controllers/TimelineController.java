package Client.controllers;

import Client.ClientApplicationContext;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import logic_core.app.dto.request.GetTimelineResponse;
import logic_core.app.dto.timeline.TimelineTweet;
import logic_core.domain.repository.TimelineType;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;


public class TimelineController {

    private static final Logger log = Logger.getLogger(TimelineController.class.getName());

    @FXML
    private ImageView currentUserAvatar;

    @FXML
    private TextField newTweetField;

    @FXML
    private Button submitTweetButton;

    @FXML
    private VBox tweetsContainer;

    @FXML
    private ImageView mediaPreview;

    @FXML
    private Button addMediaButton;

    private final ClientApplicationContext context;


    public TimelineController(ClientApplicationContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        loadCurrentUserProfile();
        loadTimelineTweets();
    }

    @FXML
    void handleSubmitTweet(ActionEvent event) {
        String tweetContent = newTweetField.getText();

        if (tweetContent == null || tweetContent.trim().isEmpty()) {
            return;
        }

        submitTweetButton.setDisable(true);

        context.getTweetService().createTweet(
                tweetContent.trim(),
                null,
                null,
                List.of(),
                null
        ).thenAccept(result ->
                Platform.runLater(() ->
                {
                    submitTweetButton.setDisable(false);

                    if (result == null || result.isFailure())
                    {
                        log.warning("Create Tweet failed : " + (result == null ? "" : result.getError()));
                        return;
                    }
                    newTweetField.clear();

                    loadTimelineTweets();
                })
        ).exceptionally(ex ->
        {
            Platform.runLater(() ->
                    submitTweetButton.setDisable(false));

            log.severe(ex.getMessage());

            return null;
        });
    }

    private void loadCurrentUserProfile() {
        // بعداً Avatar از Cache یا ProfileService خوانده می‌شود.
    }


    private void loadTimelineTweets() {
        if (!context.session().isLoggedIn()) {
            return;
        }

        UUID currentUser = context.getSnapshot().userId();

        context.getTimelineService().getTimeline(
                TimelineType.HOME,
                currentUser,
                null,
                0,
                20
        ).thenAccept(result ->
                Platform.runLater(() ->
                {
                    tweetsContainer.getChildren().clear();

                    if (result == null || result.isFailure())
                    {
                        log.warning("Timeline Error : "
                                + (result == null ? "" : result.getError()));

                        showEmptyState("Unable to load timeline.");
                        return;
                    }

                    GetTimelineResponse response =
                            result.getData();

                    if (response == null
                            || response.tweets() == null
                            || response.tweets().isEmpty())
                    {
                        showEmptyState(
                                "No posts yet! Your timeline is empty."
                        );
                        return;
                    }

                    for (TimelineTweet tweet : response.tweets())
                    {
                        addTweetCard(tweet);
                    }

                })
        ).exceptionally(ex ->
        {
            Platform.runLater(() ->
                    showEmptyState(
                            "Something went wrong while loading timeline."
                    ));

            log.severe(ex.getMessage());

            return null;
        });
    }

    private void addTweetCard(TimelineTweet tweet) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Client/fxml/TweetItem.fxml")
            );

            loader.setControllerFactory(type -> {

                if (type == TweetItemController.class) {
                    return new TweetItemController(context);
                }

                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Node node = loader.load();

            TweetItemController controller = loader.getController();
            controller.setTweet(tweet);

            controller.setOnDeleteSuccess(() -> {
                tweetsContainer.getChildren().remove(node);
                if (tweetsContainer.getChildren().isEmpty()) {
                    showEmptyState("No posts yet! Your timeline is empty.");
                }
            });

            tweetsContainer.getChildren().add(node);

        } catch (IOException e) {
            log.severe(e.getMessage());
        }
    }

    private void showEmptyState(String message)
    {
        tweetsContainer.getChildren().clear();
        Label label = new Label(message);

        label.setStyle("-fx-text-fill:#666666;" + "-fx-padding:16;");

        tweetsContainer.getChildren().add(label);
    }

    @FXML
    void handleSelectMedia()
    {

    }

    @FXML
    void handleCreatePoll()
    {

    }
}