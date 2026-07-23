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
    void showProfile(ActionEvent event) {
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

                    // تنظیم اندازه صفحه لودشده با اندازه contentArea
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
}