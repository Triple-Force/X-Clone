package Client.controllers;

import Client.ClientApplicationContext;
import Client.PasswordResetContext;
import Client.Service.AuthClientService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;

public class VerifyCodeController
{
    private static final String RESET_PASSWORD_FXML = "/Client/fxml/ResetPassword.fxml";
    private static final String LOGIN_FXML = "/Client/fxml/Login.fxml";
    private  static final String FORGOT_PASSWORD_FXML = "/Client/fxml/Forgotpassword.fxml";

    @FXML
    private TextField verificationCodeField;

    @FXML
    private Button verifyButton;

    @FXML
    private Hyperlink resendCodeLink;

    @FXML
    private Hyperlink backToLoginLink;

    // TODO : you can add Label to show error
    // TODO : you can add Label to show info(Code accepted)

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
        String code = verificationCodeField.getText().trim();

        if (code.isEmpty())
        {
            return;
        }
        else if (code.length() != 6)
        {
            return;
        }
        else
        {

        }
        String email = PasswordResetContext.getInstance().getEmail();

        if (email == null || email.isBlank())
        {
            context.navigation().navigateTo(FORGOT_PASSWORD_FXML, "X - Forgot Password");
            return;
        }


        authClientService.verifyPasswordResetCode(email, code)
                .thenAccept(result -> {
                    if (result.isSuccess())
                    {
                        PasswordResetContext.getInstance().setCode(code);

                        // TODO : show error
                        context.navigation().navigateTo(RESET_PASSWORD_FXML, "X - Login");
                    }
                    else
                    {

                        // TODO : show error

                    }
                })
                .exceptionally(ex -> {

                    // TODO : show error

                    return null;
                });
    }

    @FXML
    void handleResendCode(ActionEvent event)
    {
        // TODO : clear error

        String email = PasswordResetContext.getInstance().getEmail();
        if (email == null || email.isBlank())
        {
            // TODO : show error

            PasswordResetContext.getInstance().clear();
            context.navigation().navigateTo(FORGOT_PASSWORD_FXML, "X - Forgot Password");
            return;
        }

        authClientService.requestPasswordReset(email)
                .thenAccept(result -> {
                    if (result.isSuccess())
                    {
                        // TODO : show info
                        // showInfo("A new verification code has been sent.");

                        verificationCodeField.clear();
                        PasswordResetContext.getInstance().setCode(null);
                    }
                    else
                    {
                        // TODO : show error
                        // showError(resolveErrorMessage(result.errorCode(), result.errorMessage()));
                    }
                })
                .exceptionally(ex -> {
                    // TODO : show error
                    // showError("An unexpected error occurred: " + ex.getMessage());
                    return null;
                });
    }

    @FXML
    void handleBackToLogin(ActionEvent event)
    {
        PasswordResetContext.getInstance().clear();
        context.navigation().navigateTo(LOGIN_FXML, "X - Login");
    }
}