package Client.controllers;

import Client.ClientApplicationContext;
import Shared.Models.Tweet.Tweet;
import Shared.Models.User.User;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import logic_core.infrastructure.transport.RequestEnvelope;
import logic_core.infrastructure.transport.RequestType;
import logic_core.infrastructure.transport.ResponseEnvelope;

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

    private final ClientApplicationContext context;
    private final Gson gson = new Gson();

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

        context.networkExecutor().execute(() -> {
            try {
                log.info("Tweet action triggered locally. Content: " + tweetContent);

                /*
                 * TODO
                 *
                 * User author = new User();
                 * Tweet newTweet = Tweet.builder()
                 *         .author(author)
                 *         .content(tweetContent.trim())
                 *         .build();
                 *
                 * JsonElement payloadJson = gson.toJsonTree(newTweet);
                 *
                 * RequestEnvelope request = new RequestEnvelope(
                 *         UUID.randomUUID(),
                 *         RequestType.TWEET_CREATE,
                 *         payloadJson,
                 *         null
                 * );
                 *
                 * ResponseEnvelope response = context.socketClient().send(request);
                 */
                Platform.runLater(() -> {
                    newTweetField.clear();
                    submitTweetButton.setDisable(false);
                    loadTimelineTweets();
                });

            } catch (Exception e) {
                log.severe("Error in handleSubmitTweet: " + e.getMessage());
                Platform.runLater(() -> submitTweetButton.setDisable(false));
            }
        });
    }

    private void loadCurrentUserProfile() {
        if (context.session().isLoggedIn()) {
            UUID currentUserId = context.session().getCurrentUserId();

            /*
             * TODO
             *
             * RequestEnvelope request = new RequestEnvelope(
             *         UUID.randomUUID(),
             *         RequestType.USER_GET_PROFILE,
             *         gson.toJsonTree(currentUserId),
             *         null
             * );
             * ResponseEnvelope response = context.socketClient().send(request);
             */

            // Temporary local update for UI layout testing
            Platform.runLater(() -> {
                // TODO
                // userDisplayNameLabel.setText("Current User");
                // usernameLabel.setText("@current_user");
            });
        }
    }


    private void loadTimelineTweets() {
        context.networkExecutor().execute(() -> {
            try {
                /*
                 * TODO
                 *
                 * RequestEnvelope request = new RequestEnvelope(
                 *         UUID.randomUUID(),
                 *         RequestType.TWEET_GET_TIMELINE,
                 *         null,
                 *         null
                 * );
                 * ResponseEnvelope response = context.socketClient().send(request);
                 */

                // Temporary mock data for UI testing
                List<Tweet> mockTweets = createMockTweets();

                Platform.runLater(() -> {
                    tweetsContainer.getChildren().clear();

                    for (Tweet tweet : mockTweets) {
                        // TODO
                        // Node tweetCard = createTweetCardNode(tweet);
                        // tweetsContainer.getChildren().add(tweetCard);
                    }
                });

            } catch (Exception e) {
                log.severe("Error loading timeline tweets: " + e.getMessage());
            }
        });
    }

    // Helper method to generate dummy data for UI testing
    private List<Tweet> createMockTweets() {
        User sampleUser = new User();
        sampleUser.setDisplayName("Test User");
        sampleUser.setUsername("test_user");

        Tweet t1 = Tweet.builder()
                .author(sampleUser)
                .content("This is a mock tweet for testing the timeline layout")
                .build();

        return List.of(t1);
    }
}