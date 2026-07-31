package Client.controllers;

import Client.ClientApplicationContext;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import java.io.IOException;
import java.util.logging.Logger;

public class MainLayoutController {

    private static final Logger log = Logger.getLogger(MainLayoutController.class.getName());

    private static final String TIMELINE_FXML = "/Client/fxml/Timeline.fxml";
    private static final String MESSAGES_FXML = "/Client/fxml/Messages.fxml";
    private static final String PROFILE_FXML = "/Client/fxml/Profile.fxml";
    private static final String USER_LIST_FXML = "/Client/fxml/UserList.fxml";

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
        loadSubView(TIMELINE_FXML, null);
    }

    @FXML
    void showMessages(ActionEvent event) {
        loadSubView(MESSAGES_FXML, null);
    }

    @FXML
    public void showProfile(ActionEvent event) {
        loadSubView(PROFILE_FXML, null);
    }

    @FXML
    public void toggleTheme(ActionEvent event) {
    }

    @FXML
    public void handleSearch(ActionEvent event) {
    }

    public void showUserList(String title, String targetUsername, boolean isFollowersList) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(USER_LIST_FXML));
                loader.setControllerFactory(this::createControllerInstance);
                Parent view = loader.load();

                UserListController controller = loader.getController();
                controller.setContext(context);
                controller.loadUsers(title, targetUsername, isFollowersList);

                setContentView(view);
            } catch (IOException e) {
                log.severe("Could not load UserList view | Error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void loadSubView(String fxmlPath, InitializerCallback callback) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                loader.setControllerFactory(this::createControllerInstance);
                Parent view = loader.load();

                if (callback != null) {
                    callback.init(loader.getController());
                }

                setContentView(view);
            } catch (IOException e) {
                log.severe("Could not load FXML view from path: " + fxmlPath + " | Error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void setContentView(Parent view) {
        if (contentArea != null && view != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            if (view instanceof Pane paneView) {
                paneView.prefWidthProperty().bind(contentArea.widthProperty());
                paneView.prefHeightProperty().bind(contentArea.heightProperty());
            }
        }
    }

    private Object createControllerInstance(Class<?> controllerClass) {
        if (controllerClass == TimelineController.class) {
            return new TimelineController(context);
        } else if (controllerClass == MessagesController.class) {
            return new MessagesController(context);
        } else if (controllerClass == ProfileController.class) {
            return new ProfileController(context);
        } else if (controllerClass == UserListController.class) {
            return new UserListController();
        } else if (controllerClass == UserItemController.class) {
            return new UserItemController(context);
        }

        try {
            return controllerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not create instance of: " + controllerClass.getName(), e);
        }
    }

    private interface InitializerCallback {
        void init(Object controller);
    }
}