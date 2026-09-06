package Client.controllers;

import Client.ClientApplicationContext;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import logic_core.app.dto.response.UserSearchResponse;
import logic_core.app.dto.response.UserSummaryResponse;

import java.io.IOException;
import java.util.logging.Logger;

public class MainLayoutController {

    private static final Logger log = Logger.getLogger(MainLayoutController.class.getName());

    private static final String TIMELINE_FXML = "/Client/fxml/Timeline.fxml";
    private static final String MESSAGES_FXML = "/Client/fxml/Messages.fxml";
    private static final String PROFILE_FXML = "/Client/fxml/Profile.fxml";

   @FXML
    private TextField searchTextField;
   @FXML
    public VBox searchResultsContainer;

    @FXML
    private Pane contentArea;

    private final ClientApplicationContext context;

    public MainLayoutController(ClientApplicationContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        showTimeline(null);
    }

    @FXML
    void showTimeline(ActionEvent event) {
        loadSubView(TIMELINE_FXML);
    }

    @FXML
    void showMessages(ActionEvent event) {
        loadSubView(MESSAGES_FXML);
    }


    @FXML
    public void toggleTheme(ActionEvent event) {
    }
    @FXML
    public void handleSearch(ActionEvent event)
    {
        String query = searchTextField.getText();

        if (query == null || query.isBlank())
        {
            searchResultsContainer.getChildren().clear();
            return;
        }

        context.getUserClientService()
                .searchUsers(query, 20, 0)
                .thenAccept(result -> Platform.runLater(() ->
                {
                    searchResultsContainer.getChildren().clear();

                    if (!result.isSuccess() || result.getData() == null)
                    {
                        return;
                    }

                    for (UserSearchResponse user : result.getData())
                    {
                        try
                        {
                            FXMLLoader loader =
                                    new FXMLLoader(getClass().getResource("/Client/fxml/UserItem.fxml"));

                            loader.setControllerFactory(param ->
                                    new UserItemController(context));

                            Parent node = loader.load();

                            UserItemController controller = loader.getController();

                            controller.setUser(
                                    UserSummaryResponse.builder()
                                            .userId(user.id())
                                            .username(user.username())
                                            .displayName(user.displayName())
                                            .avatarUrl(user.avatarUrl())
                                            .verified(false)
                                            .build()
                            );

                            searchResultsContainer.getChildren().add(node);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }));
    }


    @FXML
    public void showProfile(ActionEvent event) {

        loadSubView(PROFILE_FXML);
    }

    private void loadSubView(String fxmlPath) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

                loader.setControllerFactory(this::createControllerInstance);

                Parent view = loader.load();

                if (contentArea != null && view != null) {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(view);

                    if (view instanceof Pane paneView) {
                        paneView.prefWidthProperty().bind(contentArea.widthProperty());
                        paneView.prefHeightProperty().bind(contentArea.heightProperty());
                    }
                }
            } catch (IOException e) {
                log.severe("Could not load FXML view from path: " + fxmlPath + " | Error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private Object createControllerInstance(Class<?> controllerClass) {
        if (controllerClass == TimelineController.class) {
            return new TimelineController(context);
        } else if (controllerClass == MessagesController.class) {
            return new MessagesController(context);
        } else if (controllerClass == ProfileController.class) {
            return new ProfileController(context);
        }

        try {
            return controllerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not create instance of: " + controllerClass.getName(), e);
        }
    }

    @FXML
    void showFollowersList() {
        loadUserList(true);
    }

    @FXML
    void showFollowingList() {
        loadUserList(false);
    }

    private void loadUserList(boolean followers) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Client/fxml/UserList.fxml"));

            loader.setControllerFactory(param -> {
                if (param == UserListController.class) {
                    return new UserListController(context);
                }

                try {
                    return param.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent view = loader.load();

            UserListController controller = loader.getController();

            if (followers) {
                controller.loadFollowers(context.getSnapshot().userId());
            } else {
                controller.loadFollowing(context.getSnapshot().userId());
            }

            contentArea.getChildren().setAll(view);

            if (view instanceof Pane pane) {
                pane.prefWidthProperty().bind(contentArea.widthProperty());
                pane.prefHeightProperty().bind(contentArea.heightProperty());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}