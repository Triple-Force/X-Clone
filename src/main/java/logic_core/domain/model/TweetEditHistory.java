package logic_core.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Immutable read-only record representing a single edit-history entry for a tweet.
 * Carries only the data needed by the domain/application layer without any JPA or
 * persistence coupling.
 */
public record TweetEditHistory(
        UUID id,
        String previousContent,
        OffsetDateTime createdAt
) {}
