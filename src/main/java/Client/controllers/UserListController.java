package Client.controllers;

import Client.ClientApplicationContext;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import logic_core.app.dto.response.UserSummaryResponse;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class UserListController {

    @FXML
    private Label pageTitleLabel;

    @FXML
    private VBox usersContainer;

    private ClientApplicationContext context;

    public void setContext(ClientApplicationContext context) {
        this.context = context;
    }

    public void loadUsers(String title, UUID targetId, boolean isFollowersList) {
        pageTitleLabel.setText(title);
        usersContainer.getChildren().clear();

        if (context == null) return;


        if (isFollowersList) {
            context.getFollowQueryClientService()
                    .getFollowers(targetId)
                    .thenAccept(result -> Platform.runLater(() -> {
                        if (result.isSuccess() && result.getData() != null) {
                            renderUserList(result.getData().followers());
                        } else {
                        }
                    }));
        } else {
            context.getFollowQueryClientService()
                    .getFollowings(targetId)
                    .thenAccept(result -> Platform.runLater(() -> {
                        if (result.isSuccess() && result.getData() != null) {
                            renderUserList(result.getData().users());
                        } else {
                            showEmpty();
                        }
                    }));
        }
    }

    private void showEmpty() {
        usersContainer.getChildren().clear();

        Label emptyLabel = new Label("No users found.");
        emptyLabel.setStyle("-fx-text-fill: #536471; -fx-font-size: 14px;");
        usersContainer.getChildren().add(emptyLabel);
    }

    private void renderUserList(List<UserSummaryResponse> users) {
        usersContainer.getChildren().clear();

        for (UserSummaryResponse user : users) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client/fxml/UserItem.fxml"));
                loader.setControllerFactory(param -> new UserItemController(context));
                VBox userNode = loader.load();

                UserItemController controller = loader.getController();
                controller.setUser(user);

                usersContainer.getChildren().add(userNode);
            } catch (Exception e) {
                System.err.println("Failed to load user item: " + e.getMessage());
            }
        }
    }
}