package logic_core.app.usecase.auth;

import jakarta.transaction.Transactional;
import logic_core.app.dto.request.RequestPasswordResetRequest;
import logic_core.app.dto.response.RequestPasswordResetResponse;
import logic_core.app.dto.validator.EmailValidator;
import logic_core.app.service.passwordReset.PasswordResetDeliveryPort;
import logic_core.app.service.passwordReset.PasswordResetOtpService;
import logic_core.common.exception.ValidationException;
import logic_core.common.result.Result;
import logic_core.common.util.StringNormalizer;
import logic_core.domain.model.UserModel;
import logic_core.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class RequestPasswordResetUseCase
{
    private static final String GENERIC_MESSAGE =
            "If an account exists for this email, a reset code has been sent.";

    @NonNull private final UserRepository userRepository;
    @NonNull private final PasswordResetOtpService otpService;
    @NonNull private final PasswordResetDeliveryPort deliveryPort;

    @Transactional
    public Result<RequestPasswordResetResponse> execute(RequestPasswordResetRequest request)
    {
        if (request == null)
        {
            return Result.failure("Invalid request.");
        }

        final String email = request.email();

        try
        {
            EmailValidator.validate(email);
        }
        catch (ValidationException | IllegalArgumentException e)
        {
            return Result.failure(e.getMessage());
        }


        // Anti-enumeration: same success path whether user exists or not.
        Optional<UserModel> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent())
        {
            UserModel user = userOpt.get();

            String rawOtp = otpService.issue(email, user.getId());
            deliveryPort.send(email, rawOtp);
        }

        return Result.success(new RequestPasswordResetResponse(GENERIC_MESSAGE));
    }
}
