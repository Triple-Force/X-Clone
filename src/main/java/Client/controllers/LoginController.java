package Client.controllers;

import Client.ClientApplicationContext;
import Client.Service.AuthClientService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import logic_core.app.dto.response.AuthResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController
{
    private static final String FORGOT_PASSWORD_FXML = "/Client/fxml/ForgotPassword.fxml";
    private static final String REGISTER_FXML = "/Client/fxml/Register.fxml";
    private static final String HOME_FXML = "/Client/fxml/Home.fxml";
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

    // TODO : you can add Label to show error

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
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty())
        {
            // TODO : show error
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
                                : "Login failed");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        setLoading(false);

                        // TODO ; show error

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

        // TODO : implement this method

//        if (errorLabel != null)
//        {
//            errorLabel.setText(message);
//            errorLabel.setVisible(true);
//        }
//        else
//        {
//            log.warning("UI error (no errorLabel): " + message);
//        }
    }

    private void clearError()
    {
        // TODO : implement this method

//        if (errorLabel != null)
//        {
//            errorLabel.setText("");
//            errorLabel.setVisible(false);
//        }
    }
}

