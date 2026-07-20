package logic_core.app.service.passwordReset;

public interface PasswordResetDeliveryPort
{
    void send(String email, String rawOtp);
}
