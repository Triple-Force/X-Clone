package Client.controllers;

import Client.ClientApplicationContext;
import Client.Service.AuthClientService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import logic_core.app.dto.response.AuthResponse;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RegisterController
{
    private static final Logger log = Logger.getLogger(RegisterController.class.getName());

    private static final String HOME_FXML = "/Client/fxml/Home.fxml";
    private static final String LOGIN_FXML = "/Client/fxml/Login.fxml";

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
    private Label errorLabel;

    private final ClientApplicationContext context;
    private final AuthClientService authClientService;

    public RegisterController(ClientApplicationContext context)
    {
        this.context = context;
        this.authClientService = new AuthClientService(context);
    }

    @FXML
    void handleRegister(ActionEvent event)
    {
        clearError();

        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty())
        {
            showError("Please fill in all required fields.");
            return;
        }

        if (!password.equals(confirmPassword))
        {
            showError("Passwords do not match.");
            return;
        }

        setLoading(true);

        authClientService.register(username, email, password, username)
                .thenAccept(result -> Platform.runLater(() -> {
                    setLoading(false);

                    if (result.isSuccess())
                    {
                        AuthResponse auth = result.data();
                        log.info("Register OK: " + auth.username() + " / " + auth.userId());

                        context.navigation().navigateTo(HOME_FXML, "X - Home");
                    }
                    else
                    {
                        showError(result.errorMessage() != null
                                ? result.errorMessage()
                                : "Registration failed");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        setLoading(false);

                        showError("Connection error. Please try again later.");

                        log.log(Level.SEVERE, "Register flow failed", ex);
                    });
                    return null;
                });
    }


    @FXML
    void handleBackToLogin(ActionEvent event)
    {
        context.navigation().navigateTo(LOGIN_FXML, "X - Login");
    }


    private void setLoading(boolean loading)
    {
        if (signUpButton != null)
        {
            signUpButton.setDisable(loading);
        }
        if (usernameField != null)
        {
            usernameField.setDisable(loading);
        }
        if (emailField != null)
        {
            emailField.setDisable(loading);
        }
        if (passwordField != null)
        {
            passwordField.setDisable(loading);
        }
        if (confirmPasswordField != null)
        {
            confirmPasswordField.setDisable(loading);
        }
    }

    private void showError(String message)
    {
        if (errorLabel != null)
        {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
        else
        {
            log.warning("UI error (no errorLabel bound in FXML): " + message);
        }
    }

    private void clearError()
    {
        if (errorLabel != null)
        {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        }
    }
}