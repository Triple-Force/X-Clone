package Client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button signUpButton;

    @FXML
    private Hyperlink backToLoginLink;


    @FXML
    void handleRegister(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {

        } else if (!password.equals(confirmPassword)) {

        } else {

        }
    }


    @FXML
    void handleBackToLogin(ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientLogin.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = (Stage) backToLoginLink.getScene().getWindow();

            stage.setScene(new Scene(loginRoot));
            stage.setTitle("X - Login");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}