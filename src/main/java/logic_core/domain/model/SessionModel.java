package logic_core.domain.model;

import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SessionModel
{
    private UUID id;

    private UUID userId;

    private String token;

    private OffsetDateTime expiresAt;
}
