package Client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class ForgotPasswordController {

    @FXML
    private ImageView logoImageView;

    @FXML
    private TextField emailField;

    @FXML
    private Button resetButton;

    @FXML
    private Hyperlink backToLoginLink;

    @FXML
    void handleResetPassword(ActionEvent event) {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {

        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client/VerifyCode.fxml"));
                Parent verifyRoot = loader.load();

                Stage stage = (Stage) resetButton.getScene().getWindow();
                stage.setScene(new Scene(verifyRoot));
                stage.setTitle("X - Verify Code");
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void handleBackToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client/views/Login.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = (Stage) backToLoginLink.getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.setTitle("X-Clone - Login");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}