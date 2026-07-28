package logic_core.app.security;

import logic_core.domain.model.UserModel;
import Shared.Models.Session.Session;

public record SessionUserContext(
        UserModel lockedUser,
        Session session
) {}
