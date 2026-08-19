package Client.controllers;

import Client.ClientApplicationContext;
import Client.PasswordResetContext;
import Client.Service.AuthClientService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class VerifyCodeController
{
    private static final String RESET_PASSWORD_FXML = "/Client/fxml/ResetPassword.fxml";
    private static final String LOGIN_FXML = "/Client/fxml/Login.fxml";
    private static final String FORGOT_PASSWORD_FXML = "/Client/fxml/Forgotpassword.fxml";

    @FXML
    private TextField verificationCodeField;

    @FXML
    private Button verifyButton;

    @FXML
    private Hyperlink resendCodeLink;

    @FXML
    private Hyperlink backToLoginLink;

    @FXML
    private Label errorLabel;

    @FXML
    private Label infoLabel;

    private final ClientApplicationContext context;
    private final AuthClientService authClientService;

    public VerifyCodeController(ClientApplicationContext context)
    {
        this.context = context;
        this.authClientService = new AuthClientService(context);
    }

    @FXML
    void handleVerifyCode(ActionEvent event)
    {
        clearMessages();

        String code = verificationCodeField.getText() != null ? verificationCodeField.getText().trim() : "";

        if (code.isEmpty())
        {
            showError("Please enter the verification code.");
            return;
        }
        else if (code.length() != 6)
        {
            showError("Verification code must be 6 digits.");
            return;
        }

        String email = PasswordResetContext.getInstance().getEmail();

        if (email == null || email.isBlank())
        {
            context.navigation().navigateTo(FORGOT_PASSWORD_FXML, "X - Forgot Password");
            return;
        }

        setLoading(true);

        authClientService.verifyPasswordResetCode(email, code)
                .thenAccept(result -> Platform.runLater(() -> {
                    setLoading(false);

                    if (result.isSuccess())
                    {
                        PasswordResetContext.getInstance().setCode(code);
                        context.navigation().navigateTo(RESET_PASSWORD_FXML, "X - Reset Password");
                    }
                    else
                    {
                        showError(resolveErrorMessage(result.errorCode(), result.errorMessage()));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        setLoading(false);
                        showError("Connection error: " + ex.getMessage());
                    });
                    return null;
                });
    }

    @FXML
    void handleResendCode(ActionEvent event)
    {
        clearMessages();

        String email = PasswordResetContext.getInstance().getEmail();
        if (email == null || email.isBlank())
        {
            PasswordResetContext.getInstance().clear();
            context.navigation().navigateTo(FORGOT_PASSWORD_FXML, "X - Forgot Password");
            return;
        }

        setLoading(true);

        authClientService.requestPasswordReset(email)
                .thenAccept(result -> Platform.runLater(() -> {
                    setLoading(false);

                    if (result.isSuccess())
                    {
                        showInfo("A new verification code has been sent.");
                        verificationCodeField.clear();
                        PasswordResetContext.getInstance().setCode(null);
                    }
                    else
                    {
                        showError(resolveErrorMessage(result.errorCode(), result.errorMessage()));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        setLoading(false);
                        showError("Connection error: " + ex.getMessage());
                    });
                    return null;
                });
    }

    @FXML
    void handleBackToLogin(ActionEvent event)
    {
        PasswordResetContext.getInstance().clear();
        context.navigation().navigateTo(LOGIN_FXML, "X - Login");
    }

    private String resolveErrorMessage(String errorCode, String errorMessage) {
        if (errorMessage != null && !errorMessage.isBlank()) {
            return errorMessage;
        }
        if (errorCode != null && !errorCode.isBlank()) {
            return "Verification failed: " + errorCode;
        }
        return "Invalid verification code.";
    }

    private void setLoading(boolean loading)
    {
        if (verifyButton != null) {
            verifyButton.setDisable(loading);
        }
        if (verificationCodeField != null) {
            verificationCodeField.setDisable(loading);
        }
        if (resendCodeLink != null) {
            resendCodeLink.setDisable(loading);
        }
    }

    private void showError(String message)
    {
        if (infoLabel != null) {
            infoLabel.setVisible(false);
        }
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }

    private void showInfo(String message)
    {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
        if (infoLabel != null) {
            infoLabel.setText(message);
            infoLabel.setVisible(true);
        }
    }

    private void clearMessages()
    {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        }
        if (infoLabel != null) {
            infoLabel.setText("");
            infoLabel.setVisible(false);
        }
    }
}