package Client.controllers;

import Client.ClientApplicationContext;
import Client.PasswordResetContext;
import Client.Service.AuthClientService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;


public class ForgotPasswordController
{
    private static final String VERIFY_FXML = "/Client/fxml/VerifyCode.fxml";
    private static final String LOGIN_FXML = "/Client/fxml/Login.fxml";

    @FXML
    private ImageView logoImageView;

    @FXML
    private TextField emailField;

    @FXML
    private Button resetButton;

    @FXML
    private Hyperlink backToLoginLink;

    @FXML
    private Label errorLabel;

    private final ClientApplicationContext context;
    private final AuthClientService authClientService;

    public ForgotPasswordController(ClientApplicationContext context)
    {
        this.context = context;
        this.authClientService = new AuthClientService(context);
    }

    @FXML
    void handleResetPassword(ActionEvent event)
    {
        clearError();

        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showError("Please enter your email address.");
            return;
        }
        authClientService.requestPasswordReset(email)
                .thenAccept(result -> {
                    if (result.isSuccess()) {
                        PasswordResetContext.getInstance().setEmail(email);
                        context.navigation().navigateTo(VERIFY_FXML, "X - Verify Code");
                    } else {
                        PasswordResetContext.getInstance().clear();
                        showError(resolveErrorMessage(result.errorCode(), result.errorMessage()));
                    }
                })
                .exceptionally(ex -> {
                    showError("An unexpected error occurred: " + ex.getMessage());
                    return null;
                });
    }

    @FXML
    void handleBackToLogin(ActionEvent event)
    {
        PasswordResetContext.getInstance().clear();
        context.navigation().navigateTo(LOGIN_FXML, "X - Login");
    }

    @FXML
    private String resolveErrorMessage(String errorCode, String errorMessage) {
        if (errorMessage != null && !errorMessage.isBlank()) {
            return errorMessage;
        }
        if (errorCode != null && !errorCode.isBlank()) {
            return "Request failed: " + errorCode;
        }
        return "Failed to request password reset.";
    }

    private void showError(String message)
    {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }

    private void clearError()
    {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        }
    }
}