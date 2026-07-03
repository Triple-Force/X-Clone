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

public class VerifyCodeController {

    @FXML
    private TextField verificationCodeField;

    @FXML
    private Button verifyButton;

    @FXML
    private Hyperlink resendCodeLink;

    @FXML
    private Hyperlink backToLoginLink;


    @FXML
    void handleVerifyCode(ActionEvent event) {
        String code = verificationCodeField.getText().trim();

        if (code.isEmpty()) {

        } else if (code.length() != 6) {

        } else {

        }
    }

    @FXML
    void handleResendCode(ActionEvent event) {
    }

    @FXML
    void handleBackToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client/Login.fxml"));
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