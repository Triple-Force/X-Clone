package logic_core.app.security;

import logic_core.domain.model.SessionModel;
import logic_core.domain.model.UserModel;

public record SessionUserContext(
        UserModel lockedUser,
        SessionModel session
) {}
