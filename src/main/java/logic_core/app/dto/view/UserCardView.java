package logic_core.app.dto.view;

import java.util.UUID;

public record UserCardView(
        UUID id,
        String username,
        String displayName,
        String avatarUrl,
        boolean verified,
        boolean following, boolean blocked
) {}
