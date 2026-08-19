package logic_core.app.dto.request;

import java.util.List;
import java.util.UUID;

public record DeleteAccountRequest(
        UUID useId,
        String sessionToken,
        String password
) {}
