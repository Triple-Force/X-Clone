package Client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageUploadDialogController {

    @FXML private HBox imagePreviewContainer;

    private final List<File> selectedFiles = new ArrayList<>();
    private boolean confirmed = false;

    @FXML
    private void handleChooseImages() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Images");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(imagePreviewContainer.getScene().getWindow());

        if (files != null && !files.isEmpty()) {
            for (File file : files) {
                if (!selectedFiles.contains(file)) {
                    selectedFiles.add(file);
                    addImageToPreview(file);
                }
            }
        }
    }

    private void addImageToPreview(File file) {
        VBox card = new VBox(5);
        card.setStyle("-fx-alignment: center; -fx-border-color: #cfd9de; -fx-border-radius: 8; -fx-padding: 5;");

        ImageView imageView = new ImageView(new Image(file.toURI().toString()));
        imageView.setFitWidth(90);
        imageView.setFitHeight(90);
        imageView.setPreserveRatio(true);

        Button removeBtn = new Button("❌");
        removeBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 10px; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> {
            selectedFiles.remove(file);
            imagePreviewContainer.getChildren().remove(card);
        });

        card.getChildren().addAll(imageView, removeBtn);
        imagePreviewContainer.getChildren().add(card);
    }

    @FXML
    private void handleCancel() {
        selectedFiles.clear();
        confirmed = false;
        closeStage();
    }

    @FXML
    private void handleConfirm() {
        confirmed = true;
        closeStage();
    }

    public List<File> getSelectedFiles() {
        return confirmed ? selectedFiles : new ArrayList<>();
    }

    private void closeStage() {
        Stage stage = (Stage) imagePreviewContainer.getScene().getWindow();
        stage.close();
    }
}