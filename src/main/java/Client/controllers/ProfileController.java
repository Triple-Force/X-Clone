package Client.controllers;

import Client.ClientApplicationContext;
import Shared.Models.Tweet.Tweet;
import Shared.Models.User.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class ProfileController {

    private static final Logger log = Logger.getLogger(ProfileController.class.getName());

    @FXML
    private Label headerNameLabel;

    @FXML
    private ImageView profileAvatar;

    @FXML
    private Button editProfileButton;

    @FXML
    private Label displayNameLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label bioLabel;

    @FXML
    private VBox userTweetsContainer;

    private final ClientApplicationContext context;

    public ProfileController(ClientApplicationContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        loadUserProfileData();
        loadUserTweets();
    }

    @FXML
    void handleEditProfile(ActionEvent event) {
        log.info("Edit Profile button clicked.");
        // TODO
    }

    private void loadUserProfileData() {
        if (!context.session().isLoggedIn()) {
            return;
        }

        UUID currentUserId = context.session().getCurrentUserId();

        context.networkExecutor().execute(() -> {
            try {
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

                // Temporary mock user data for UI layout testing
                User mockUser = createMockUser();

                Platform.runLater(() -> {
                    headerNameLabel.setText(mockUser.getDisplayName());
                    displayNameLabel.setText(mockUser.getDisplayName());
                    usernameLabel.setText("@" + mockUser.getUsername());
                    // bioLabel.setText(mockUser.getBio()); // Uncomment if User model has bio field
                });

            } catch (Exception e) {
                log.severe("Error loading profile data: " + e.getMessage());
            }
        });
    }

    private void loadUserTweets() {
        context.networkExecutor().execute(() -> {
            try {
                /*
                 * TODO
                 *
                 * RequestEnvelope request = new RequestEnvelope(
                 *         UUID.randomUUID(),
                 *         RequestType.TWEET_GET_USER_TWEETS,
                 *         null,
                 *         null
                 * );
                 * ResponseEnvelope response = context.socketClient().send(request);
                 */

                // Temporary mock data for testing profile posts list
                List<Tweet> mockUserTweets = createMockUserTweets();

                Platform.runLater(() -> {
                    userTweetsContainer.getChildren().clear();

                    if (mockUserTweets.isEmpty()) {
                        Label emptyLabel = new Label("You haven't posted anything yet.");
                        emptyLabel.setStyle("-fx-text-fill: #666666;");
                        userTweetsContainer.getChildren().add(emptyLabel);
                    } else {
                        for (Tweet tweet : mockUserTweets) {
                            // TODO
                            // Node tweetCard = createTweetCardNode(tweet);
                            // userTweetsContainer.getChildren().add(tweetCard);
                        }
                    }
                });

            } catch (Exception e) {
                log.severe("Error loading user tweets: " + e.getMessage());
            }
        });
    }

    private User createMockUser() {
        User user = new User();
        user.setDisplayName("");
        user.setUsername("");
        return user;
    }

    private List<Tweet> createMockUserTweets() {
        User mockUser = createMockUser();

        return List.of();
    }
}