package Client.controllers;

import Client.ClientApplicationContext;
import Client.Service.AuthClientService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import logic_core.app.dto.response.AuthResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController
{
    private static final String FORGOT_PASSWORD_FXML = "/Client/fxml/Forgotpassword.fxml";
    private static final String REGISTER_FXML = "/Client/fxml/Register.fxml";
    private static final String HOME_FXML = "/Client/fxml/MainLayout.fxml";
    public ImageView logoImageView;
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink signUpLink;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private Label errorLabel;

    private static final Logger log = Logger.getLogger(LoginController.class.getName());
    private final ClientApplicationContext context;
    private final AuthClientService authClientService;

    public LoginController(ClientApplicationContext context)
    {
        this.context = context;
        this.authClientService = new AuthClientService(context);
    }

    @FXML
    void handleLogin(ActionEvent event)
    {
        clearError();

        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty())
        {
            showError("Please enter both username and password.");
            return;
        }

        setLoading(true);

        authClientService.login(username, password)
                .thenAccept(result -> Platform.runLater(() -> {
                    setLoading(false);

                    if (result.isSuccess())
                    {
                        AuthResponse auth = result.data();
                        log.info("Login OK: " + auth.username() + " / " + auth.userId());

                        context.navigation().navigateTo(HOME_FXML, "X - Home");
                    }
                    else
                    {
                        showError(result.errorMessage() != null
                                ? result.errorMessage()
                                : "Invalid username or password.");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        setLoading(false);

                        showError("Connection error. Please try again later.");

                        log.log(Level.SEVERE, "Login flow failed", ex);
                    });
                    return null;
                });
    }


    @FXML
    void handleSignUp(ActionEvent event)
    {
        context.navigation().navigateTo(REGISTER_FXML, "X - Register");
    }

    @FXML
    void handleForgotPassword(ActionEvent event)
    {
        context.navigation().navigateTo(FORGOT_PASSWORD_FXML, "X - Forgot Password");
    }

    private void setLoading(boolean loading)
    {
        if (loginButton != null)
        {
            loginButton.setDisable(loading);
        }
        if (usernameField != null)
        {
            usernameField.setDisable(loading);
        }
        if (passwordField != null)
        {
            passwordField.setDisable(loading);
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

