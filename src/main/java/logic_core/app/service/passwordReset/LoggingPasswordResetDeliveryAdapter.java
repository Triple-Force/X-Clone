package logic_core.app.service.passwordReset;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoggingPasswordResetDeliveryAdapter implements PasswordResetDeliveryPort
{
    @Override
    public void send(@NonNull String email, @NonNull String rawOtp)
    {
        System.out.println("[PASSWORD_RESET_OTP] email=" + email + " code=" + rawOtp);
    }
}